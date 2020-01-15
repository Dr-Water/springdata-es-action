package com.ratel.es.controller;

import com.ratel.es.entity.Poem;
import com.ratel.es.repository.PoemRepository;
import org.elasticsearch.index.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;

/**
 * @业务描述：
 * @package_name： com.ratel.es.controller
 * @project_name： springdata-es-action
 * @author： ratelfu@qq.com
 * @create_time： 2020-01-12 15:44
 * @copyright (c) ratelfu 版权所有
 */
@RestController
@RequestMapping("poem")
public class PoemController {

    @Autowired
    private PoemRepository PoemRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping("findByTitle")
    public Object findByName(@RequestParam("title")String title,@PageableDefault Pageable pageable){
        return PoemRepository.findByTitle(title, pageable);
    }

















    /**
     * 单字符串模糊查询，默认排序。将从所有字段中查找包含传来的word分词后字符串的数据集
     */
    @RequestMapping("/singleWord")
    public Object singleTitle(String word, @PageableDefault Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(word)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }
    /**
     * 单字符串模糊查询，默认排序。将从所有字段中查找包含传来的word分词后字符串的数据集
     */
    @RequestMapping("/key")
    public Object singleTitle2(String key, @PageableDefault Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(key)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }
    /**
     * 单字符串模糊查询，单字段排序。
     */
    @RequestMapping("/singleWord1")
    public Object singlePoem(String word, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(word)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }

    /**
     * 单字段对某字符串模糊查询
     */
    @RequestMapping("/singleMatch")
    public Object singleMatch(String content, Integer userId, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("content", content)).withPageable(pageable).build();
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("userId", userId)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }

    /**
     * 单字段对某短语进行匹配查询，短语分词的顺序会影响结果
     */
    @RequestMapping("/singlePhraseMatch")
    public Object singlePhraseMatch(String content, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchPhraseQuery("content", content)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }

    /**
     * 多字段匹配
     */
    @RequestMapping("/multiMatch")
    public Object singleUserId(String title, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(multiMatchQuery(title, "title", "content")).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }


    /**
     * 多字段匹配
     */
    @RequestMapping("/multiMatch2")
    public Object singleUserId2(String title, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(multiMatchQuery(title, "title", "content").operator(Operator.AND).minimumShouldMatch("75%")).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Poem.class);
    }

}
