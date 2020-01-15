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
 * @create_time： 2020-01-12 15:34
 * @copyright (c) ratelfu 版权所有
 */


@Data
@ToString
@Document(indexName="mypoems",type="poem",indexStoreType="fs",shards=5,replicas=1,refreshInterval="-1")
public class Poem{
    /**
     * 主键id
     */
    @Id
    private String id;

    /**
     * 词牌名，或者叫标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     *用户id
     */
    private int userId;

    /**
     *权重
     */
    private int weight;


}
