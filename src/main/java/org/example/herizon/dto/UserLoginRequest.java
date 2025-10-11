package org.example.herizon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户登录请求DTO
 * <p>
 * 封装用户登录时需要提供的凭证信息
 * 支持使用用户名或邮箱作为登录标识
 *
 * @author Kokoa
 */
@Data
public class UserLoginRequest{
    /**
     * 登录标识
     * <p>
     * 可以是用户名或邮箱地址
     * 系统会自动识别输入类型
     * <p>
     * 添加多种可能的字段名映射以支持不同的前端请求格式
     */
    @JsonProperty ("identifier")
    private String identifier;

    /**
     * 邮箱字段（备选字段名）
     * 如果前端发送的是email字段，也会被映射到identifier
     */
    @JsonProperty ("email")
    private String email;

    /**
     * 用户名字段（备选字段名）
     * 如果前端发送的是username字段，也会被映射到identifier
     */
    @JsonProperty ("username")
    private String username;

    /**
     * 用户名或邮箱字段（前端兼容字段）
     * 如果前端发送的是usernameOrEmail字段，也会被映射到identifier
     */
    @JsonProperty ("usernameOrEmail")
    private String usernameOrEmail;

    /**
     * 用户密码
     */
    @JsonProperty ("password")
    private String password;

    /**
     * 获取实际的登录标识
     * 优先级：identifier > email > username > usernameOrEmail
     */
    public String getActualIdentifier(){
        if( identifier!=null && !identifier.trim().isEmpty() ){
            return identifier;
        }
        if( email!=null && !email.trim().isEmpty() ){
            return email;
        }
        if( username!=null && !username.trim().isEmpty() ){
            return username;
        }
        if( usernameOrEmail!=null && !usernameOrEmail.trim().isEmpty() ){
            return usernameOrEmail;
        }
        return null;
    }
}