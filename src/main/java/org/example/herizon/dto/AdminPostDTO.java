package org.example.herizon.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员帖子管理DTO
 *
 * @author Kokoa
 */
@Data
public class AdminPostDTO {
    private Long id;
    private String title;
    private String content;
    private String username;
    private Integer postType;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer reportCount;
    private LocalDateTime createdAt;
}