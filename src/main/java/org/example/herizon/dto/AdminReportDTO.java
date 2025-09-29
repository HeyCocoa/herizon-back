package org.example.herizon.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员举报审核DTO
 *
 * @author Kokoa
 */
@Data
public class AdminReportDTO {
    private Long reportId;
    private Long targetId;
    private String targetType;
    private String reportReason;
    private String reporterUsername;
    private String targetContent;
    private String targetAuthorUsername;
    private LocalDateTime reportTime;
    private String status;
}