package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table ("user_actions")
public class UserAction{
    @Id (keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private Long targetId;
    private String targetType;

    /**
     * 行为类型: 0=点赞, 1=收藏, 2=分享, 3=举报, 4=关注
     */
    private Integer actionType;

    /**
     * 额外数据(JSON格式，如举报原因)
     */
    private String extraData;

    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}