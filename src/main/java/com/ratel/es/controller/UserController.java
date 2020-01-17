package com.ratel.es.controller;

import com.ratel.es.entity.User;
import com.ratel.es.repository.UserRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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











}
