package com.ratel.es.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * @业务描述：
 * @package_name： com.ratel.es.entity
 * @project_name： springdata-es-action
 * @author： ratelfu@qq.com
 * @create_time： 2020-01-12 20:36
 * @copyright (c) ratelfu 版权所有
 */
@Data
@ToString
@Document(indexName="myes",type="myuser",refreshInterval="-1")
public class User {
    @Id
    private Integer id;
    private String es_first_name;
    private String last_name;
    private  Integer age;
    private String es_about;

}
