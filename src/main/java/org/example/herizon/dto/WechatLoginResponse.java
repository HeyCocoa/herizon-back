package org.example.herizon.dto;

import lombok.Data;

/**
 * 微信登录响应DTO
 * <p>
 * 微信登录成功后返回给前端的数据格式
 * 包含Token和用户基本信息
 *
 * @author Kokoa
 */
@Data
public class WechatLoginResponse {
    /**
     * Token
     * <p>
     * 用于后续API调用的身份认证
     */
    private String token;

    /**
     * 用户基本信息
     * <p>
     * 脱敏后的用户信息，供前端使用
     */
    private UserInfo userInfo;

    /**
     * 是否为新用户
     * <p>
     * true=首次登录（新注册），false=老用户登录
     */
    private Boolean isNewUser;

    /**
     * Token过期时间（时间戳）
     * <p>
     * 前端可用于判断是否需要刷新Token
     */
    private Long tokenExpiration;

    /**
     * 登录成功时间（时间戳）
     */
    private Long loginTime;

    /**
     * 用户信息内部类
     * <p>
     * 包含前端需要的用户基本信息
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户ID（前端兼容性字段）
         */
        private Long id;

        /**
         * 微信openid（脱敏处理）
         */
        private String openid;

        /**
         * 用户名（可能为空）
         */
        private String username;

        /**
         * 用户昵称
         */
        private String nickname;

        /**
         * 用户邮箱（可能为空）
         */
        private String email;

        /**
         * 用户头像URL
         */
        private String avatar;

        /**
         * 用户角色：0=体验用户，1=正式用户，2=管理员
         */
        private Integer role;

        /**
         * 用户角色描述
         */
        private String roleDesc;

        /**
         * 注册来源：1=普通注册，2=微信小程序，3=微信App
         */
        private Integer registerSource;

        /**
         * 用户状态：0=正常，1=禁用
         */
        private Integer status;

        /**
         * 注册时间（时间戳）
         */
        private Long registrationTime;

        /**
         * 是否已认证（体验用户升级为正式用户）
         */
        private Boolean isVerified;

        /**
         * 是否完成身份认证问卷
         */
        private Boolean questionnaireCompleted;

        /**
         * 对openid进行脱敏处理
         * <p>
         * 只显示前4位和后4位，中间用*代替
         *
         * @param openid 原始openid
         * @return 脱敏后的openid
         */
        public void setOpenidMasked(String openid) {
            if (openid != null && openid.length() > 8) {
                this.openid = openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
            } else {
                this.openid = openid;
            }
        }

        /**
         * 根据角色代码获取角色描述
         *
         * @param role 角色代码
         * @return 角色描述
         */
        public void setRoleDesc(Integer role) {
            switch (role) {
                case 0:
                    this.roleDesc = "体验用户";
                    break;
                case 1:
                    this.roleDesc = "正式用户";
                    break;
                case 2:
                    this.roleDesc = "管理员";
                    break;
                default:
                    this.roleDesc = "未知角色";
            }
        }
    }
}