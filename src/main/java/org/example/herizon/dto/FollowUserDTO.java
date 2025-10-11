package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;
    private Boolean isVerified;
    private Boolean isFollowing;
    private Boolean isMutualFollow;
    private Boolean isSelf;
    private LocalDateTime followTime;
    private Long followersCount;
    private Long followingCount;
    private Long postsCount;
}
