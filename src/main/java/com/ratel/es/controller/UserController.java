package com.ratel.es.controller;

import com.ratel.es.entity.User;
import com.ratel.es.repository.UserRepository;
import com.ratel.es.result.HighlightResultMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @业务描述：
 * @package_name： com.ratel.es.controller
 * @project_name： springdata-es-action
 * @author： ratelfu@qq.com
 * @create_time： 2020-01-12 20:40
 * @copyright (c) ratelfu 版权所有
 */
@RestController
@RequestMapping("user")
public class UserController {

    private static final Logger log= LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ElasticsearchTemplate estemplate;



    @RequestMapping("queryString")
    public Object queryString(String key, @PageableDefault Pageable pageable){
        return userRepository.queryString(key, pageable);
    }


    /**
     * 模拟将mysql的数据导入es中
     * @return
     */
    @RequestMapping("mysqlToEs")
    public String mysqlToEs(){

        log.info("开始将数据导入es");
        long startTime = System.currentTimeMillis();
        //模拟从mysql中取到user的集合
        List<User> userListMysql = getUserListForMysql();
        try {
            int count = 0;
            if (estemplate.indexExists(User.class)) {
                // 如果存在index则先删除，避免数据冗余或者数据刷新不完整
                estemplate.deleteIndex(User.class);
                log.info("删除旧index");
            }
            estemplate.createIndex(User.class);
            //解决高版本的@Field注解失效问题
            estemplate.putMapping(User.class);

            log.info("新建index");
            ArrayList<IndexQuery> queries = new ArrayList<>();

            for (User entity : userListMysql ) {
                IndexQuery indexQuery = new IndexQuery();
                //这个地方就是设置主键id，当前你也可以换成其他字段,或者uuid,这个地方默认使用id作为_id的值，即使实体类上不加@Id注解
                //indexQuery.setId(UUID.randomUUID().toString());
                indexQuery.setId(entity.getId().toString());
                indexQuery.setObject(entity);
                indexQuery.setIndexName("myes");
                indexQuery.setType("myuser");
                queries.add(indexQuery);
                if (count % 500 == 0) {
                    estemplate.bulkIndex(queries);
                    queries.clear();
                }
                count++;
            }
            if (queries.size() > 0) {
                estemplate.bulkIndex(queries);
                log.info("导入完成，耗时：" + (System.currentTimeMillis() - startTime) + "ms"+"导入总条数："+count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("导入失败");
        }
        return "导入成功！";
    }

    public List<User> getUserListForMysql(){
        ArrayList<User> userListMysql = new ArrayList<>();
        for (int i = 1001; i <1060 ; i++) {
           userListMysql.add(new User(i,"张","三"+i,i,"简介"+i)) ;
        }
        return userListMysql;
    }

    @RequestMapping("/flushByIndex")
    public String flushEsByIndex(){
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setIndex("myes");
        deleteQuery.setType("myuser");
        //这个查询一定要有，查询中不设置任何条件即认为是删除当前索引的所有记录
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        deleteQuery.setQuery(boolQueryBuilder);
        estemplate.delete(deleteQuery);
        return "清空es数据成功";
    }


    /**
     * 高亮显示
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getUsers")
    public Page<User> query( String keyword, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        String preTags = "<span style=\"color:#F56C6C\">";
        String postTags = "</span>";
        HighlightBuilder.Field authorField = new HighlightBuilder.Field("firstName").preTags(preTags).postTags(postTags);
        HighlightBuilder.Field textField = new HighlightBuilder.Field("lastName").preTags(preTags).postTags(postTags);

        SearchQuery searchQuery = null;
        if (!StringUtils.isEmpty(keyword)) {
            searchQuery = new NativeSearchQueryBuilder()
                    //.withPageable(new QPageRequest(pageNum - 1, pageSize))
                    .withPageable(PageRequest.of(pageNum - 1, pageSize))
                    .withQuery(QueryBuilders.multiMatchQuery(keyword, "firstName", "lastName"))
                    .withHighlightFields(authorField, textField)
                    .withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC))
                    .build();
        } else {
            searchQuery = new NativeSearchQueryBuilder()
                     .withPageable(PageRequest.of(pageNum - 1, pageSize))
                     .withQuery(QueryBuilders.matchAllQuery())
                     .withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC))
                     .build();
        }
        Page<User> users = estemplate.queryForPage(searchQuery, User.class, new HighlightResultMapper());
        return  users;

    }

    /**
     * 高亮显示
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getUserList")
    public Page<User> query2( String keyword, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        String preTags = "<span style=\"color:#F56C6C\">";
        String postTags = "</span>";
        HighlightBuilder.Field firstName = new HighlightBuilder.Field("firstName").preTags(preTags).postTags(postTags);
        HighlightBuilder.Field lastName = new HighlightBuilder.Field("lastName").preTags(preTags).postTags(postTags);
        HighlightBuilder.Field[] fields =new  HighlightBuilder.Field[2];
        fields[0]= firstName;
        fields[1]= lastName;
        SearchQuery searchQuery = null;
        if (!StringUtils.isEmpty(keyword)) {
            searchQuery = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageNum - 1, pageSize))
                    .withQuery(QueryBuilders.multiMatchQuery(keyword, "firstName", "lastName"))
                    .withHighlightFields(fields)
                    .withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC))
                    .build();
        } else {
            searchQuery = new NativeSearchQueryBuilder()
                    .withPageable(PageRequest.of(pageNum - 1, pageSize))
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC))
                    .build();
        }
        Page<User> users = estemplate.queryForPage(searchQuery, User.class, new HighlightResultMapper());
        return  users;

    }

    /**
     * 高亮显示
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getUserList2")
    public Page<User> query3( String keyword, Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }

        //获取QueryBuilder
        BoolQueryBuilder querryBuilder = this.getQuerryBuilder(keyword,"三1006");
        NativeSearchQueryBuilder nativeSearchQueryBuilder = this.getWildcardQuery(keyword, "myes", "myuser");
        //关联分页、过滤器
        nativeSearchQueryBuilder
                .withIndices("myes")
                .withTypes("myuser")
                //添加bool过滤器
                .withFilter(querryBuilder)
                .withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC))
                //添加分页条件
                .withPageable(PageRequest.of(pageNum - 1, pageSize));

        //查询结果
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();

        log.info(searchQuery.getFilter().toString());
        System.out.println("=================");
        //log.info(searchQuery.get);
        System.out.println(nativeSearchQueryBuilder.build().getQuery().toString());
        AggregatedPage<User> esEntityList = estemplate.queryForPage(nativeSearchQueryBuilder.build(), User.class);

        return esEntityList;

    }

    /**
     * 获取对应filter类
     * @return
     */
    public BoolQueryBuilder getQuerryBuilder(String inner,String outer) {

        //生成bool查询过滤器 must、should、mustnot
        BoolQueryBuilder menuBoolQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder InoolQueryBuilder = new BoolQueryBuilder();
                                        // 对于搜索的key 不进行分词
        InoolQueryBuilder.should(new MatchPhraseQueryBuilder("lastName", outer));
        //.filter在里层
        menuBoolQueryBuilder.filter(InoolQueryBuilder);
        //这个在外层
        menuBoolQueryBuilder.must(new MatchPhraseQueryBuilder("lastName", outer));

        return menuBoolQueryBuilder;
    }

    /**
     * 获取WildcardQuery
     *  模糊查询 *key*
     * @param key
     * @return
     */
    public NativeSearchQueryBuilder getWildcardQuery(String key, String index, String type) {
        //数据权限,这里是先should，里面包含must
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder
                //索引名
                .withIndices(index)
                //类型名
                .withTypes(type);
        //关键词
        if (key != null && !key.isEmpty() && !key.equals("")) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                                                        // 模糊查询 类似mysql的like  %key% 不分词
                boolQueryBuilder.should(QueryBuilders.wildcardQuery("firstName","*"+key+"*"));
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        }
        return nativeSearchQueryBuilder;
    }









}
