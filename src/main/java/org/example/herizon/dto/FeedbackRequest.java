package org.example.herizon.dto;

import lombok.Data;

/**
 * 反馈请求DTO
 * <p>
 * 用于接收用户提交的反馈信息
 *
 * @author Claude Code
 */
@Data
public class FeedbackRequest {

    private String type;

    private String title;

    private String content;

    private String contact;

    private String userAgent;

    /**
     * 验证请求参数有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        // 类型不能为空
        if (type == null || type.trim().isEmpty()) {
            return false;
        }

        // 内容不能为空
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        return true;
    }
}
