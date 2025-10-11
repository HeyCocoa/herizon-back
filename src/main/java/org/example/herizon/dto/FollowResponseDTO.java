package org.example.herizon.dto;

import lombok.Data;

/**
 * 关注操作响应DTO
 * <p>
 * 用于POST /api/actions/follow接口的响应
 * 返回关注状态和目标用户的粉丝数
 *
 * @author Kokoa
 */
@Data
public class FollowResponseDTO {
    /**
     * 是否已关注
     */
    private Boolean isFollowing;

    /**
     * 目标用户的粉丝数
     */
    private Long followersCount;
}
