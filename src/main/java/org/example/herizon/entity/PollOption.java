package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投票选项实体类
 * <p>
 * 对应数据库表：poll_options
 * <p>
 * 功能说明：
 * - 用于存储投票帖（post_type=1）的投票选项
 * - 每个投票帖包含2-5个投票选项
 * - 通过post_id关联到posts表
 * - 使用display_order控制选项显示顺序
 * <p>
 * 字段说明：
 * - id: 主键，自动自增
 * - postId: 关联的帖子ID（外键关联到posts.id）
 * - optionText: 投票选项文本内容（最大200字符）
 * - displayOrder: 选项显示顺序（从1开始）
 * - createdAt: 创建时间
 * - deleted: 逻辑删除标记（0=正常, 1=删除）
 *
 * @author Kokoa
 * @since 2025-10-01
 */
@Data
@Table("poll_options")
public class PollOption {
    /**
     * 投票选项ID
     * 主键，自动自增
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 帖子ID
     * 关联到posts表的id字段
     * 一个投票帖包含多个投票选项
     */
    private Long postId;

    /**
     * 投票选项文本
     * 例如："支持"、"反对"、"中立"等
     * 最大长度200字符
     */
    private String optionText;

    /**
     * 显示顺序
     * 用于控制前端显示时的排列顺序
     * 从1开始，按数字升序排列
     */
    private Integer displayOrder;

    /**
     * 创建时间
     * 投票选项的创建时间（与帖子创建时间一致）
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     * 0 = 正常状态
     * 1 = 已删除
     * <p>
     * 注意：投票选项一般不单独删除，而是随帖子一起删除
     */
    private Integer deleted;
}
