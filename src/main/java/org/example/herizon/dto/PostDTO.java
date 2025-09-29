package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子数据传输对象
 * <p>
 * 用于API响应的帖子数据封装，相比Post实体增加了：
 * - 用户名信息
 * - 标签列表
 * - 当前用户的操作状态（是否点赞、收藏）
 *
 * @author Kokoa
 */
@Data
public class PostDTO{
    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 发帖用户ID
     */
    private Long userId;

    /**
     * 发帖用户名
     */
    private String username;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子类型：0=普通帖，1=投票帖，2=违规公示帖
     */
    private Integer postType;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 帖子标签列表
     */
    private List<String> tags;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isCollected;
}