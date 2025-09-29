package org.example.herizon.dto;

import lombok.Data;

/**
 * 创建评论请求DTO
 * <p>
 * 封装创建新评论时需要提供的信息
 * 支持创建顶级评论和回复评论
 *
 * @author Kokoa
 */
@Data
public class CreateCommentRequest {
    /**
     * 所属帖子ID
     * <p>
     * 必填字段，指定评论所属的帖子
     */
    private Long postId;

    /**
     * 评论内容
     * <p>
     * 必填字段，评论的文本内容
     * 长度限制：1-1000个字符
     */
    private String content;

    /**
     * 父评论ID
     * <p>
     * 可选字段，如果提供则创建回复评论
     * 如果为null或空，则创建顶级评论
     */
    private Long parentId;
}