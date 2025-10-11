package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端反馈信息视图
 */
@Data
public class FeedbackSummaryDTO {

    private Long id;

    private String type;

    private String content;

    private String status;

    private String statusLabel;

    private String adminReply;

    private String contact;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
