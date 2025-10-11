package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈实体类
 * <p>
 * 用户意见反馈和问题报告
 * 支持匿名反馈和分类管理
 *
 * @author Claude Code
 */
@Data
@Table("feedback")
public class Feedback {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 提交用户ID（可为空，支持匿名反馈）
     */
    private Long userId;

    /**
     * 反馈类型: suggestion=建议, bug=问题, complaint=投诉
     */
    private String type;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈详细内容
     */
    private String content;

    /**
     * 联系方式（可选）
     */
    private String contact;

    /**
     * 用户环境信息（可选）
     */
    private String userAgent;

    /**
     * 处理状态: submitted=已提交, processing=处理中, resolved=已解决, closed=已关闭
     */
    private String status;

    /**
     * 管理员回复内容
     */
    private String adminReply;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 优先级: 1=低, 2=中, 3=高, 4=紧急
     */
    private Integer priority;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}