package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体
 */
@Data
@Table("user_follow")
public class UserFollow {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 关注人ID
     */
    private Long followerId;

    /**
     * 被关注人ID
     */
    private Long followeeId;

    /**
     * 关注时间
     */
    private LocalDateTime createdAt;
}
