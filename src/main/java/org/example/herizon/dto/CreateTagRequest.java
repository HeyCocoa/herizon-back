package org.example.herizon.dto;

import lombok.Data;

/**
 * 创建标签请求DTO
 * <p>
 * 封装创建或更新标签时需要提供的信息
 *
 * @author Kokoa
 */
@Data
public class CreateTagRequest {
    /**
     * 标签名称
     * <p>
     * 要求：1-20个字符，不能包含特殊字符
     * 在同一时间内标签名称必须唯一
     */
    private String name;

    /**
     * 标签描述
     * <p>
     * 可选字段，用于说明标签的用途和适用范围
     * 最大长度200个字符
     */
    private String description;
}