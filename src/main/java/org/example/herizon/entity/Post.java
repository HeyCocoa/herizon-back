package org.example.herizon.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.ColumnMask;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Table ("posts")
public class Post{
    @Id (keyType = KeyType.Auto)
    private Long id;

    private Long userId;
    private String title;
    private String content;

    /**
     * 帖子类型: 0=普通帖, 1=投票帖, 2=违规公示帖
     */
    private Integer postType;

    private Integer viewCount;
    private Integer likeCount;
    private Integer shareCount;
    private Integer collectCount;

    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 图片URL列表（存储为JSON数组）
     * 系统变更后支持最多3张图片上传
     */
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private List<String> imageUrls;

    /**
     * 视频URL
     * <p>
     * 该功能已废弃（2025-10-01系统变更）
     * 原因：简化发布流程，移除视频发布功能，仅保留图文和投票功能
     * 保留该字段是为了：
     * 1. 向后兼容历史数据（避免历史视频帖子数据丢失）
     * 2. 避免数据库迁移风险（无需ALTER TABLE操作）
     * 3. 为未来可能的功能恢复预留空间
     * <p>
     * 使用@Column(ignore = true)告诉MyBatis-Flex忽略此字段，避免SQL语句包含此列
     *
     * @deprecated 自2025-10-01起不再支持视频发布功能，该字段仅用于历史数据兼容
     */
    @Deprecated
    @Column(ignore = true)
    private String videoUrl;

    /**
     * 视频封面图URL
     * <p>
     * 该功能已废弃（2025-10-01系统变更）
     * 与videoUrl字段配套使用，同样保留用于历史数据兼容
     * <p>
     * 使用@Column(ignore = true)告诉MyBatis-Flex忽略此字段，避免SQL语句包含此列
     *
     * @deprecated 自2025-10-01起不再支持视频发布功能，该字段仅用于历史数据兼容
     */
    @Deprecated
    @Column(ignore = true)
    private String videoCover;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 状态: 0=正常, 1=删除, 2=审核中
     */
    private Integer status;

    /**
     * 逻辑删除标记: 0=正常, 1=删除
     */
    private Integer deleted;
}