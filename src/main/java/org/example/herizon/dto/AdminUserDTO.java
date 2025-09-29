package org.example.herizon.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员用户管理DTO
 *
 * @author Kokoa
 */
@Data
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private Integer role;
    private String roleDescription;
    private String questionnaireData;
    private LocalDateTime createdAt;
    private Integer postCount;
    private Integer commentCount;
    private Boolean isPendingVerification;
}