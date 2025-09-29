package org.example.herizon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 * <p>
 * 用于API响应的标签数据封装，相比Tag实体增加了：
 * - 最近使用时间信息
 * - 使用该标签的最新帖子标题示例
 *
 * @author Kokoa
 */
@Data
public class TagDTO {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称，唯一
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 使用该标签的帖子数量
     */
    private Integer postCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最近使用时间
     * <p>
     * 表示最后一次有帖子使用该标签的时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 使用该标签的最新帖子标题示例
     * <p>
     * 用于在标签列表中显示相关内容预览
     */
    private String latestPostTitle;

    /**
     * 是否为热门标签
     * <p>
     * 基于帖子数量和最近使用情况计算
     */
    private Boolean isHot;

    /**
     * 计算是否为热门标签
     * <p>
     * 简单逻辑：帖子数量 >= 10 且最近30天内有使用
     *
     * @return true=热门标签，false=普通标签
     */
    public Boolean getIsHot() {
        if (postCount == null || postCount < 10) {
            return false;
        }

        if (lastUsedAt == null) {
            return false;
        }

        // 检查是否在最近30天内有使用
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return lastUsedAt.isAfter(thirtyDaysAgo);
    }
}