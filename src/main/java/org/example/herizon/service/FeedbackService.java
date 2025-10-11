package org.example.herizon.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herizon.dto.FeedbackAdminViewDTO;
import org.example.herizon.dto.FeedbackRequest;
import org.example.herizon.dto.FeedbackSummaryDTO;
import org.example.herizon.entity.Feedback;
import org.example.herizon.entity.User;
import org.example.herizon.enums.FeedbackStatus;
import org.example.herizon.mapper.FeedbackMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 反馈服务
 * <p>
 * 处理用户意见反馈的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    /**
     * 提交反馈
     *
     * @param request 反馈请求
     * @param userId  当前用户ID
     * @return 保存后的反馈实体
     */
    @Transactional
    public Feedback submitFeedback(FeedbackRequest request, Long userId) {
        User user = requireExistingUser(userId);
        ensureOfficialUser(user);

        Feedback feedback = new Feedback();
        feedback.setUserId(user.getId());
        feedback.setType(request.getType().trim());
        feedback.setTitle(resolveTitle(request));
        feedback.setContent(request.getContent().trim());
        feedback.setContact(trimToNull(request.getContact()));
        feedback.setUserAgent(trimToNull(request.getUserAgent()));
        feedback.setStatus(FeedbackStatus.PENDING.getCode());
        feedback.setAdminReply(null);
        feedback.setAdminId(null);
        feedback.setPriority(determinePriority(request.getType()));
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        feedback.setDeleted(0);

        feedbackMapper.insert(feedback);

        log.info("用户反馈已提交 - ID: {}, 类型: {}, 用户ID: {}",
                feedback.getId(), feedback.getType(), user.getId());

        return feedback;
    }

    /**
     * 查询当前用户的反馈记录
     *
     * @param userId 用户ID
     * @return 反馈列表
     */
    @Transactional(readOnly = true)
    public List<FeedbackSummaryDTO> listUserFeedback(Long userId) {
        User user = requireExistingUser(userId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", user.getId())
                .and("deleted = 0")
                .orderBy("created_at desc");

        List<Feedback> feedbackList = feedbackMapper.selectListByQuery(queryWrapper);
        return feedbackList.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 管理员查看全量反馈
     *
     * @param adminId      管理员ID
     * @param statusFilter 状态过滤（pending/resolved）
     * @return 反馈列表
     */
    @Transactional(readOnly = true)
    public List<FeedbackAdminViewDTO> listFeedbackForAdmin(Long adminId, String statusFilter) {
        requireAdminUser(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("deleted = 0")
                .orderBy("created_at desc");

        String normalizedStatus = FeedbackStatus.normalizeOrNull(statusFilter);
        if (normalizedStatus != null) {
            queryWrapper.and("status = ?", normalizedStatus);
        }

        List<Feedback> feedbacks = feedbackMapper.selectListByQuery(queryWrapper);
        Map<Long, User> userMap = loadUsers(feedbacks);

        return feedbacks.stream()
                .map(feedback -> toAdminDto(feedback, userMap.get(feedback.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 管理员回复反馈
     *
     * @param feedbackId   反馈ID
     * @param replyContent 回复内容
     * @param adminId      管理员ID
     */
    @Transactional
    public void replyFeedback(Long feedbackId, String replyContent, Long adminId) {
        User admin = requireAdminUser(adminId);

        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }

        Feedback feedback = feedbackMapper.selectOneById(feedbackId);
        if (feedback == null || (feedback.getDeleted() != null && feedback.getDeleted() == 1)) {
            throw new IllegalArgumentException("反馈不存在");
        }

        feedback.setAdminReply(replyContent.trim());
        feedback.setAdminId(admin.getId());
        feedback.setStatus(FeedbackStatus.RESOLVED.getCode());
        feedback.setUpdatedAt(LocalDateTime.now());

        feedbackMapper.update(feedback);

        log.info("管理员回复反馈 - 反馈ID: {}, 管理员ID: {}", feedbackId, adminId);
    }

    /**
     * 将实体转换为用户端视图
     *
     * @param feedback 反馈实体
     * @return 用户端视图
     */
    public FeedbackSummaryDTO toSummaryView(Feedback feedback) {
        return toSummaryDto(feedback);
    }

    private FeedbackSummaryDTO toSummaryDto(Feedback feedback) {
        FeedbackSummaryDTO dto = new FeedbackSummaryDTO();
        dto.setId(feedback.getId());
        dto.setType(feedback.getType());
        dto.setContent(feedback.getContent());
        String statusCode = FeedbackStatus.normalize(feedback.getStatus());
        dto.setStatus(statusCode);
        dto.setStatusLabel(FeedbackStatus.labelOf(statusCode));
        dto.setAdminReply(feedback.getAdminReply());
        dto.setContact(feedback.getContact());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        return dto;
    }

    private FeedbackAdminViewDTO toAdminDto(Feedback feedback, User user) {
        FeedbackAdminViewDTO dto = new FeedbackAdminViewDTO();
        dto.setId(feedback.getId());
        dto.setUserId(feedback.getUserId());
        if (user != null) {
            dto.setUsername(resolveDisplayName(user));
            dto.setUserRole(user.getRole());
            dto.setUserRoleLabel(resolveRoleLabel(user.getRole()));
        }
        dto.setType(feedback.getType());
        dto.setContent(feedback.getContent());
        String statusCode = FeedbackStatus.normalize(feedback.getStatus());
        dto.setStatus(statusCode);
        dto.setStatusLabel(FeedbackStatus.labelOf(statusCode));
        dto.setAdminReply(feedback.getAdminReply());
        dto.setContact(feedback.getContact());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        return dto;
    }

    private Map<Long, User> loadUsers(List<Feedback> feedbacks) {
        Map<Long, User> result = new HashMap<>();
        for (Feedback feedback : feedbacks) {
            Long userId = feedback.getUserId();
            if (userId == null || result.containsKey(userId)) {
                continue;
            }
            User user = userMapper.selectOneById(userId);
            if (user != null) {
                result.put(userId, user);
            }
        }
        return result;
    }

    private User requireExistingUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录后再提交反馈");
        }
        User user = userMapper.selectOneById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new IllegalArgumentException("用户不存在或已被删除");
        }
        return user;
    }

    private User requireAdminUser(Long adminId) {
        User admin = requireExistingUser(adminId);
        if (admin.getRole() == null || admin.getRole() != 2) {
            throw new IllegalStateException("仅管理员可以执行该操作");
        }
        return admin;
    }

    private void ensureOfficialUser(User user) {
        Integer role = user.getRole();
        if (role == null || role < 1) {
            throw new IllegalStateException("请先完成身份认证后再提交反馈");
        }
    }

    private String resolveTitle(FeedbackRequest request) {
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            return request.getTitle().trim();
        }
        String content = request.getContent();
        if (content == null) {
            return "用户反馈";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= 20) {
            return trimmed;
        }
        return trimmed.substring(0, 20) + "...";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int determinePriority(String type) {
        if (type == null) {
            return 2;
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if ("bug".equals(normalized) || "complaint".equals(normalized)) {
            return 3;
        }
        return 2;
    }

    private String resolveDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
    }

    private String resolveRoleLabel(Integer role) {
        if (role == null) {
            return "未知";
        }
        return switch (role) {
            case 2 -> "管理员";
            case 1 -> "正式用户";
            default -> "体验用户";
        };
    }
}
