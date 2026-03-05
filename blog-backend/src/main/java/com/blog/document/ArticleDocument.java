package com.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * Elasticsearch 文章文档（用于高亮分词搜索）
 *
 * @author blog
 */
@Data
@Document(indexName = "blog_article")
public class ArticleDocument {

    @Id
    private Long id;
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Keyword)
    private Integer status;
    @Field(type = FieldType.Date)
    private LocalDateTime publishTime;
}
