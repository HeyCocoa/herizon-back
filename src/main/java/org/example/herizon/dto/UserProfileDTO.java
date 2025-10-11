package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户资料DTO
 * <p>
 * 用于API响应的用户资料数据封装，相比User实体：
 * - 隐藏了密码等敏感信息
 * - 增加了角色描述和状态描述
 * - 可选择性地包含私有信息（用于当前用户查询）
 *
 * @author Kokoa
 */
@Data
public class UserProfileDTO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名（唯一标识，内部使用）
     */
    private String username;

    /**
     * 用户昵称（对外显示用）
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 邮箱地址（仅对当前用户显示）
     */
    private String email;

    /**
     * 用户角色：0=体验用户, 1=正式用户, 2=管理员
     */
    private Integer role;

    /**
     * 角色描述
     */
    private String roleDescription;

    /**
     * 身份认证问卷数据（仅对当前用户显示）
     */
    private String questionnaireData;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 用户发帖数量
     */
    private Integer postCount;

    /**
     * 用户获得的总点赞数
     */
    private Integer totalLikes;

    /**
     * 用户收藏数量
     */
    private Integer collectCount;

    /**
     * ��˿����
     */
    private Long followersCount;

    /**
     * ��ע����
     */
    private Long followingCount;

    /**
     * ��ǰ�û��Ƿ��ѹ�ע
     */
    private Boolean isFollowing;

    /**
     * �Ƿ�Ϊ�����ע
     */
    private Boolean isMutualFollow;

    /**
     * 会话token（仅登录时返回）
     */
    private String token;

    /**
     * 是否为当前用户的完整资料
     * <p>
     * true：包含私有信息（邮箱、问卷数据等）
     * false：仅公开信息
     */
    private Boolean isFullProfile;

    /**
     * 获取角色的中文描述
     *
     * @return 角色描述
     */
    public String getRoleDescription() {
        if (role == null) return "未知";
        switch (role) {
            case 0:
                return "体验用户";
            case 1:
                return "正式用户";
            case 2:
                return "管理员";
            default:
                return "未知";
        }
    }
}