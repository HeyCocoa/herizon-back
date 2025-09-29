package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论数据传输对象
 * <p>
 * 用于API响应的评论数据封装，相比Comment实体增加了：
 * - 评论者用户名信息
 * - 帖子标题信息
 * - 子评论列表（嵌套结构）
 * - 回复数量统计
 *
 * @author Kokoa
 */
@Data
public class CommentDTO {
    /**
     * 评论ID
     */
    private Long id;

    /**
     * 所属帖子ID
     */
    private Long postId;

    /**
     * 所属帖子标题
     */
    private String postTitle;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 评论者用户名
     */
    private String username;

    /**
     * 父评论ID，顶级评论为null
     */
    private Long parentId;

    /**
     * 父评论作者用户名（用于显示"回复@xxx"）
     */
    private String parentUsername;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 评论状态：0=正常, 1=删除
     */
    private Integer status;

    /**
     * 回复数量
     * <p>
     * 该评论收到的回复数量
     */
    private Integer replyCount;

    /**
     * 子评论列表
     * <p>
     * 仅在查询评论详情或树形结构时填充
     * 为避免数据量过大，通常只包含前几条子评论
     */
    private List<CommentDTO> replies;

    /**
     * 评论层级深度
     * <p>
     * 0=顶级评论，1=一级回复，2=二级回复...
     * 用于前端显示缩进效果
     */
    private Integer level;

    /**
     * 是否为已删除评论的占位符
     * <p>
     * 当原评论被删除但有子评论时，显示为占位符
     */
    private Boolean isPlaceholder;

    /**
     * 当前用户是否可以删除此评论
     * <p>
     * 用户可以删除自己的评论，管理员可以删除任何评论
     */
    private Boolean canDelete;

    /**
     * 计算评论的显示内容
     * <p>
     * 如果评论已删除且为占位符，返回提示文本
     *
     * @return 显示内容
     */
    public String getDisplayContent() {
        if (Boolean.TRUE.equals(isPlaceholder)) {
            return "该评论已被删除";
        }
        return content;
    }

    /**
     * 获取层级样式类名
     * <p>
     * 用于前端CSS样式控制
     *
     * @return CSS类名
     */
    public String getLevelClass() {
        if (level == null || level <= 0) {
            return "comment-level-0";
        }
        // 最多显示3级缩进
        int displayLevel = Math.min(level, 3);
        return "comment-level-" + displayLevel;
    }
}