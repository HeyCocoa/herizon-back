package org.example.herizon.dto;

import lombok.Data;

/**
 * 管理员回复反馈请求体
 */
@Data
public class FeedbackReplyRequest {

    /**
     * 回复内容
     */
    private String reply;
}
