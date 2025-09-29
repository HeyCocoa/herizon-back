package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table ("posts")
public class Post{
    @Id (keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private String title;
    private String content;

    /**
     * 帖子类型: 0=普通帖, 1=投票帖, 2=违规公示帖
     */
    private Integer postType;

    private Integer viewCount;
    private Integer likeCount;
    private Integer shareCount;
    private Integer collectCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 状态: 0=正常, 1=删除, 2=审核中
     */
    private Integer status;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}