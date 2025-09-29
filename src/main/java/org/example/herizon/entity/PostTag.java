package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 帖子标签关联实体类
 * <p>
 * 对应数据库表：post_tags
 * 实现帖子和标签的多对多关系
 *
 * @author Kokoa
 */
@Data
@Table ("post_tags")
public class PostTag{
    /**
     * 关联记录ID，主键自增
     */
    @Id (keyType = KeyType.Auto)
    private Long id;

    /**
     * 帖子ID，外键关联posts表
     */
    private Long postId;

    /**
     * 标签ID，外键关联tags表
     */
    private Long tagId;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}