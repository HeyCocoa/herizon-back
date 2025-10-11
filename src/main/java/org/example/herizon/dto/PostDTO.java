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
     * 发帖用户名（唯一标识，内部使用）
     */
    private String username;

    /**
     * 发帖用户昵称（对外显示用）
     */
    private String nickname;

    /**
     * 发帖用户头像URL
     */
    private String userAvatar;

    /**
     * 发帖用户角色：0=体验用户, 1=正式用户, 2=管理员
     */
    private Integer userRole;

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
    private List<TagDTO> tags;

    /**
     * 图片URL列表(最多3张)
     */
    private List<String> imageUrls;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isCollected;

    /**
     * 投票选项列表（仅当postType=1时有值）
     * 包含选项文本和投票数统计
     */
    private List<PollOptionDTO> pollOptions;

    /**
     * 当前用户的投票选择（仅当postType=1时有值）
     * 存储用户选择的投票选项ID，未投票时为null
     */
    private Long myVote;
    /**
     * ��ǰ�û��Ƿ��ѹ�ע����
     */
    private Boolean isAuthorFollowed;

}