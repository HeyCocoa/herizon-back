package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户投票记录实体类
 * <p>
 * 对应数据库表：user_votes
 * <p>
 * 功能说明：
 * - 记录用户对投票帖的投票行为
 * - 每个用户对每个投票帖只能投一票（通过唯一索引uk_user_post保证）
 * - 支持投票撤回（通过逻辑删除实现）
 * - 用于统计各选项的投票数和查询用户的投票选择
 * <p>
 * 字段说明：
 * - id: 主键，自动自增
 * - userId: 投票用户ID（外键关联到users.id）
 * - postId: 投票帖ID（外键关联到posts.id）
 * - optionId: 所选选项ID（外键关联到poll_options.id）
 * - createdAt: 投票时间
 * - deleted: 逻辑删除标记（0=正常, 1=删除/撤回）
 * <p>
 * 业务规则：
 * 1. 唯一约束：(user_id, post_id, deleted) 确保每人每帖只能投一票
 * 2. 投票撤回：设置deleted=1，允许用户重新投票
 * 3. 统计投票：通过COUNT查询同一optionId的记录数
 *
 * @author Kokoa
 * @since 2025-10-01
 */
@Data
@Table("user_votes")
public class UserVote {
    /**
     * 投票记录ID
     * 主键，自动自增
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 投票用户ID
     * 关联到users表的id字段
     * 标识哪个用户进行了投票
     */
    private Long userId;

    /**
     * 投票帖ID
     * 关联到posts表的id字段
     * 标识对哪个投票帖进行投票
     */
    private Long postId;

    /**
     * 所选选项ID
     * 关联到poll_options表的id字段
     * 标识用户选择了哪个投票选项
     */
    private Long optionId;

    /**
     * 投票时间
     * 记录用户投票的准确时间
     * 用于投票时间统计和排序
     */
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     * 0 = 有效投票
     * 1 = 已撤回投票
     * <p>
     * 业务意义：
     * - 设置为1表示用户撤回了投票
     * - 撤回后可以重新投票（创建新记录）
     * - 唯一索引 uk_user_post(user_id, post_id, deleted) 确保每人每帖只有一个有效投票
     */
    private Integer deleted;
}
