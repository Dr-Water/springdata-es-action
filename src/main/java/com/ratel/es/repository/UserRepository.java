package com.ratel.es.repository;



import com.ratel.es.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
  *@业务描述：
  *@author： fuyongnan
  *@create_time： 2020/1/12 15:40
  */  
public interface UserRepository extends ElasticsearchRepository<User, String> {

    @Query("{\"query_string\" : {\"fields\" : [\"es_*\"],\"query\" : \"?\"}}")
    Page<User> queryString(String key, Pageable pageable);
}
