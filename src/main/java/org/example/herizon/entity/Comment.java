package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 * <p>
 * 对应数据库表：comments
 * 支持嵌套评论结构，通过parentId实现父子关系
 *
 * @author Kokoa
 */
@Data
@Table ("comments")
public class Comment{
    /**
     * 评论ID，主键自增
     */
    @Id (keyType = KeyType.Auto)
    private Long id;

    /**
     * 所属帖子ID
     */
    private Long postId;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 父评论ID，用于实现嵌套评论，顶级评论为null
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 状态: 0=正常, 1=删除
     */
    private Integer status;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}