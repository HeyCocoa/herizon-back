package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端反馈信息视图
 */
@Data
public class FeedbackAdminViewDTO {

    private Long id;

    private Long userId;

    private String username;

    private Integer userRole;

    private String userRoleLabel;

    private String type;

    private String content;

    private String status;

    private String statusLabel;

    private String adminReply;

    private String contact;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
