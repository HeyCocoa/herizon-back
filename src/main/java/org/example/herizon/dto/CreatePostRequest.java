package org.example.herizon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建帖子请求DTO
 * <p>
 * 用于接收客户端创建帖子的请求数据
 * <p>
 * 修订历史：
 * - 2025-10-01: 修复前后端字段匹配问题
 *   1. postType改为type，匹配前端字段名
 *   2. tags改为tagIds，使用标签ID而非名称（RESTful最佳实践）
 *   3. 新增imageUrls字段，支持图片列表（最多3张）
 *   4. 新增isAnonymous字段，支持匿名发布
 *   5. 新增pollOptions字段，支持投票功能（type=1时使用）
 *
 * @author Kokoa
 */
@Data
@Schema(description = "创建帖子请求")
public class CreatePostRequest{
    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 帖子内容
     */
    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 帖子类型：0=普通帖，1=投票帖，2=违规公示帖
     * 字段名从postType改为type，与前端保持一致
     */
    @Schema(description = "帖子类型：0=普通帖，1=投票帖，2=违规公示帖", example = "0")
    private Integer type;

    /**
     * 标签ID列表
     * 从tags(String[])改为tagIds(Long[])，使用标签ID而非名称
     * 优势：避免标签名称冲突和变更问题，符合RESTful最佳实践
     */
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    /**
     * 图片URL列表，最多3张
     * 新增字段：支持前端上传的图片列表
     */
    @Schema(description = "图片URL列表，最多3张")
    @Size(max = 3, message = "最多只能上传3张图片")
    private List<String> imageUrls;

    /**
     * 是否匿名发布
     * 新增字段：支持用户匿名发布内容
     */
    @Schema(description = "是否匿名发布", example = "false")
    private Boolean isAnonymous;

    /**
     * 投票选项列表（仅当type=1时需要）
     * 新增字段：支持投票功能，包含2-5个选项
     */
    @Schema(description = "投票选项列表（仅当type=1时需要）")
    @Size(min = 2, max = 5, message = "投票选项必须在2-5个之间")
    private List<String> pollOptions;
}