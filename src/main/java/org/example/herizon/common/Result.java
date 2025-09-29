package org.example.herizon.common;

import lombok.Data;

/**
 * 统一API响应封装类
 * <p>
 * 用于封装所有API接口的返回结果，提供统一的响应格式：
 * {
 * "code": 200,        // 状态码：200=成功，4xx=客户端错误，5xx=服务器错误
 * "message": "success", // 响应消息
 * "data": {}          // 具体数据，泛型类型T
 * }
 *
 * @param <T> 响应数据的泛型类型
 * @author Kokoa
 */
@Data
public class Result<T>{
    /**
     * 响应状态码
     * 200: 成功
     * 4xx: 客户端错误
     * 5xx: 服务器错误
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（无数据）
     *
     * @return 成功响应对象
     */
    public static <T> Result<T> success(){
        return success(null);
    }

    /**
     * 创建错误响应（默认500状态码）
     *
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message){
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建错误响应（自定义状态码）
     *
     * @param code    状态码
     * @param message 错误消息
     * @return 错误响应对象
     */
    public static <T> Result<T> error(Integer code, String message){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}