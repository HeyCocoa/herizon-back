package org.example.herizon.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建帖子请求DTO
 * <p>
 * 用于接收客户端创建帖子的请求数据
 *
 * @author Kokoa
 */
@Data
public class CreatePostRequest{
    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子类型：0=普通帖，1=投票帖，2=违规公示帖
     */
    private Integer postType;

    /**
     * 帖子标签列表
     */
    private List<String> tags;
}