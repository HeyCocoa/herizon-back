package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * <p>
 * 对应数据库表：tags
 * 用于帖子分类和主题标记，通过post_tags表与帖子关联
 *
 * @author Kokoa
 */
@Data
@Table ("tags")
public class Tag{
    /**
     * 标签ID，主键自增
     */
    @Id (keyType = KeyType.Auto)
    private Long id;

    /**
     * 标签名称，唯一
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 使用该标签的帖子数量
     */
    private Integer postCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}