package com.ratel.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName="myes",type="myuser",refreshInterval="-1")
public class User {
    @Id // 这个id加不加都不影响导入
    private Integer id;
    private String firstName;
    private String lastName;
    private  Integer age;
    //type 为字段类型 字段名首字母不能大写 例如不能写成： About 不然有可能往es中导入不了数据
    @Field(type = FieldType.Keyword)
    private String about;
}
