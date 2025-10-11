package org.example.herizon.dto;

/**
 * 审核用户请求DTO
 * 用于接收管理员审核用户身份认证的请求参数
 */
public class VerifyUserRequest {
    /**
     * 是否通过审核
     * true - 通过审核，升级为正式用户(role=1)
     * false - 拒绝审核
     */
    private Boolean approved;

    /**
     * 审核意见或拒绝原因
     * approved=true时为审核意见（可选）
     * approved=false时为拒绝原因（建议填写）
     */
    private String reason;

    // Getters and Setters
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
