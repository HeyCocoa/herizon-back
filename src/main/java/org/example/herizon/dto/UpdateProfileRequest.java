package org.example.herizon.dto;

import lombok.Data;

/**
 * 用户资料更新请求
 *
 * @author
 */
@Data
public class UpdateProfileRequest {

    /**
     * 用户昵称，可选。
     */
    private String nickname;

    /**
     * 用户头像 URL，可选。
     */
    private String avatar;
}
