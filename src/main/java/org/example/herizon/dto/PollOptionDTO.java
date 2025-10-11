package org.example.herizon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投票选项数据传输对象
 * <p>
 * 用于API响应中展示投票选项信息，包含：
 * - 选项ID和文本
 * - 投票数统计
 * - 投票占比（前端计算）
 * <p>
 * 使用场景：
 * 1. 帖子详情API：返回投票帖的所有选项及投票统计
 * 2. 投票结果展示：显示各选项的得票数和占比
 *
 * @author Kokoa
 * @since 2025-10-01
 */
@Data
@Schema(description = "投票选项数据DTO")
public class PollOptionDTO {
    /**
     * 投票选项ID
     */
    @Schema(description = "投票选项ID", example = "1")
    private Long id;

    /**
     * 投票选项文本
     * 例如："支持"、"反对"、"中立"
     */
    @Schema(description = "投票选项文本", example = "支持")
    private String optionText;

    /**
     * 该选项的投票数
     * 实时统计user_votes表中选择该选项的记录数
     */
    @Schema(description = "该选项的投票数", example = "128")
    private Integer voteCount;

    /**
     * 显示顺序
     * 用于前端按顺序展示选项（从1开始）
     */
    @Schema(description = "显示顺序", example = "1")
    private Integer displayOrder;
}
