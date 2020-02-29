package com.ratel.es.controller;

import com.ratel.es.entity.User;
import com.ratel.es.repository.UserRepository;
import com.ratel.es.result.HighlightResultMapper;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
public class UserTestController {

    private static final Logger log= LoggerFactory.getLogger(UserTestController.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ElasticsearchTemplate estemplate;

    @GetMapping("/and")
    public Page<User> query( Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("about","中华人民共和国"))
                                        .must(QueryBuilders.termQuery("firstName","张")))
                    .withPageable(PageRequest.of(pageNum - 1, pageSize))
                    .build();
        System.out.println(searchQuery.getQuery());
        Page<User> page = estemplate.queryForPage(searchQuery, User.class);
        return  page;
    }

    @GetMapping("/or")
    public Page<User> query2( Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery("about","中华人民共和国"))
                        .should(QueryBuilders.termQuery("firstName","王")))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        System.out.println(searchQuery.getQuery());
        Page<User> page = estemplate.queryForPage(searchQuery, User.class);
        return  page;
    }

    @GetMapping("/andOr")
    public Page<User> query3( Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("firstName","张"))
                                .must(QueryBuilders.boolQuery()
                                        .should(QueryBuilders.termQuery("firstName","王"))
                                        .should(QueryBuilders.termQuery("about","中华人民共和国公民")))
                )
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        System.out.println(searchQuery.getQuery());
        Page<User> page = estemplate.queryForPage(searchQuery, User.class);
        return  page;
    }

    @GetMapping("/like")
    public Page<User> query4( Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wildcardQuery("about","*中华*"))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        System.out.println(searchQuery.getQuery());
        Page<User> page = estemplate.queryForPage(searchQuery, User.class);
        return  page;
    }

    @GetMapping("/in")
    public Page<User> query5( Integer pageNum, Integer pageSize) {
        if(pageNum == null || pageNum <= 0){
            pageNum = 1;
        }
        if(pageSize == null || pageSize <= 0){
            pageSize = 5;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery("about","中华人民共和国"))
                        .should(QueryBuilders.termQuery("about","中华人民共和国公民")))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();
        System.out.println(searchQuery.getQuery());
        Page<User> page = estemplate.queryForPage(searchQuery, User.class);
        return  page;
    }










}
