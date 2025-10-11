package org.example.herizon.enums;

import lombok.Getter;

import java.util.Locale;

/**
 * 反馈状态枚举，统一后端内部编码及展示文案
 */
@Getter
public enum FeedbackStatus {

    PENDING("pending", "未处理"),
    RESOLVED("resolved", "已解决");

    private final String code;
    private final String label;

    FeedbackStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据任意字符串解析反馈状态
     *
     * @param rawCode 传入的状态值
     * @return 反馈状态枚举
     */
    public static FeedbackStatus fromCode(String rawCode) {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            return PENDING;
        }
        String normalized = rawCode.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "pending", "submitted", "processing", "new" -> PENDING;
            case "resolved", "closed", "done", "completed" -> RESOLVED;
            default -> PENDING;
        };
    }

    /**
     * 获取统一的状态编码
     *
     * @param rawCode 任意状态值
     * @return 内部状态编码
     */
    public static String normalize(String rawCode) {
        return fromCode(rawCode).code;
    }

    /**
     * 获取中文展示文案
     *
     * @param rawCode 任意状态值
     * @return 中文状态文案
     */
    public static String labelOf(String rawCode) {
        return fromCode(rawCode).label;
    }

    /**
     * 校验输入并返回标准编码，非法值抛出异常
     *
     * @param rawCode 输入状态
     * @return 标准编码或 null（当输入为空）
     */
    public static String normalizeOrNull(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String trimmed = rawCode.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "pending", "submitted", "processing", "new" -> PENDING.code;
            case "resolved", "closed", "done", "completed" -> RESOLVED.code;
            default -> throw new IllegalArgumentException("无效的反馈状态: " + rawCode);
        };
    }
}
