package org.example.herizon.dto;

import lombok.Data;

/**
 * 用户统计数据DTO
 * <p>
 * 包含用户的发帖数、关注数、粉丝数、获赞数、被收藏数等统计信息
 * 用于GET /api/users/{userId}/stats接口的响应
 *
 * @author Kokoa
 */
@Data
public class UserStatsDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 发帖数量
     */
    private Long postsCount;

    /**
     * 关注数量
     */
    private Long followingCount;

    /**
     * 粉丝数量
     */
    private Long followersCount;

    /**
     * 获赞总数（帖子获赞）
     */
    private Long likesCount;

    /**
     * 被收藏总数（帖子被收藏）
     */
    private Long collectsCount;
}
