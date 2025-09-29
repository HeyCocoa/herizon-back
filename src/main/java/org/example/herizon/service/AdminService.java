package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.AdminPostDTO;
import org.example.herizon.dto.AdminReportDTO;
import org.example.herizon.dto.AdminUserDTO;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.User;
import org.example.herizon.entity.UserAction;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserActionMapper;
import org.example.herizon.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员服务类
 *
 * @author Kokoa
 */
@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserActionMapper userActionMapper;

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(Long adminId) {
        User admin = userMapper.selectOneById(adminId);
        if (admin == null || admin.getRole() != 2) {
            throw new RuntimeException("无管理员权限");
        }
    }

    /**
     * 获取待审核用户列表
     */
    public PageResult<AdminUserDTO> getPendingUsers(Integer current, Integer size, Long adminId) {
        validateAdminPermission(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("role = 0")
                .and("questionnaire_data IS NOT NULL")
                .and("deleted = 0")
                .orderBy("created_at DESC");

        Page<User> page = userMapper.paginate(Page.of(current, size), queryWrapper);
        List<AdminUserDTO> userDTOs = page.getRecords().stream()
                .map(this::convertToAdminUserDTO)
                .collect(Collectors.toList());

        return PageResult.of(userDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 审核用户身份认证
     */
    @Transactional
    public void verifyUser(Long userId, Boolean approved, String reason, Long adminId) {
        validateAdminPermission(adminId);

        User user = userMapper.selectOneById(userId);
        if (user == null || user.getRole() != 0) {
            throw new RuntimeException("用户不存在或不需要审核");
        }

        if (approved) {
            user.setRole(1); // 升级为正式用户
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 获取待处理举报列表
     */
    public PageResult<AdminReportDTO> getPendingReports(Integer current, Integer size, Long adminId) {
        validateAdminPermission(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("action_type = 3")
                .and("deleted = 0")
                .orderBy("created_at DESC");

        Page<UserAction> page = userActionMapper.paginate(Page.of(current, size), queryWrapper);
        List<AdminReportDTO> reportDTOs = page.getRecords().stream()
                .map(this::convertToAdminReportDTO)
                .collect(Collectors.toList());

        return PageResult.of(reportDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 处理举报
     */
    @Transactional
    public void handleReport(Long reportId, String action, String reason, Long adminId) {
        validateAdminPermission(adminId);

        UserAction report = userActionMapper.selectOneById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        if ("approve".equals(action)) {
            // 删除被举报内容
            if ("post".equals(report.getTargetType())) {
                Post post = postMapper.selectOneById(report.getTargetId());
                if (post != null) {
                    post.setStatus(1);
                    postMapper.update(post);
                }
            }
        }

        // 标记举报已处理
        report.setDeleted(1);
        userActionMapper.update(report);
    }

    /**
     * 删除帖子
     */
    @Transactional
    public void deletePost(Long postId, String reason, Boolean publishNotice, Long adminId) {
        validateAdminPermission(adminId);

        Post post = postMapper.selectOneById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        post.setStatus(1);
        postMapper.update(post);

        if (publishNotice) {
            // 创建违规公示帖
            Post noticePost = new Post();
            noticePost.setUserId(adminId);
            noticePost.setTitle("违规内容处理公示");
            noticePost.setContent("违规内容已被删除，原因：" + reason);
            noticePost.setPostType(2);
            noticePost.setCreatedAt(LocalDateTime.now());
            noticePost.setUpdatedAt(LocalDateTime.now());
            postMapper.insert(noticePost);
        }
    }

    /**
     * 修改用户角色
     */
    @Transactional
    public void changeUserRole(Long userId, Integer newRole, Long adminId) {
        validateAdminPermission(adminId);

        User user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 获取平台统计数据
     */
    public Object getStatistics(Long adminId) {
        validateAdminPermission(adminId);

        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        long totalUsers = userMapper.selectCountByQuery(QueryWrapper.create().where("deleted = 0"));
        long trialUsers = userMapper.selectCountByQuery(QueryWrapper.create().where("role = 0 AND deleted = 0"));
        long verifiedUsers = userMapper.selectCountByQuery(QueryWrapper.create().where("role = 1 AND deleted = 0"));

        // 帖子统计
        long totalPosts = postMapper.selectCountByQuery(QueryWrapper.create().where("deleted = 0"));
        long activePosts = postMapper.selectCountByQuery(QueryWrapper.create().where("status = 0 AND deleted = 0"));

        // 举报统计
        long pendingReports = userActionMapper.selectCountByQuery(
            QueryWrapper.create().where("action_type = 3 AND deleted = 0"));

        stats.put("totalUsers", totalUsers);
        stats.put("trialUsers", trialUsers);
        stats.put("verifiedUsers", verifiedUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("activePosts", activePosts);
        stats.put("pendingReports", pendingReports);

        return stats;
    }

    /**
     * 获取所有用户
     */
    public PageResult<AdminUserDTO> getAllUsers(Integer current, Integer size, Integer role, Long adminId) {
        validateAdminPermission(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create().where("deleted = 0");
        if (role != null) {
            queryWrapper.and("role = ?", role);
        }
        queryWrapper.orderBy("created_at DESC");

        Page<User> page = userMapper.paginate(Page.of(current, size), queryWrapper);
        List<AdminUserDTO> userDTOs = page.getRecords().stream()
                .map(this::convertToAdminUserDTO)
                .collect(Collectors.toList());

        return PageResult.of(userDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 获取所有帖子
     */
    public PageResult<AdminPostDTO> getAllPosts(Integer current, Integer size, Integer status, Long adminId) {
        validateAdminPermission(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create().where("deleted = 0");
        if (status != null) {
            queryWrapper.and("status = ?", status);
        }
        queryWrapper.orderBy("created_at DESC");

        Page<Post> page = postMapper.paginate(Page.of(current, size), queryWrapper);
        List<AdminPostDTO> postDTOs = page.getRecords().stream()
                .map(this::convertToAdminPostDTO)
                .collect(Collectors.toList());

        return PageResult.of(postDTOs, page.getTotalRow(), (long) current, (long) size);
    }

    private AdminUserDTO convertToAdminUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setIsPendingVerification(user.getRole() == 0 && user.getQuestionnaireData() != null);
        return dto;
    }

    private AdminReportDTO convertToAdminReportDTO(UserAction report) {
        AdminReportDTO dto = new AdminReportDTO();
        dto.setReportId(report.getId());
        dto.setTargetId(report.getTargetId());
        dto.setTargetType(report.getTargetType());
        dto.setReportTime(report.getCreatedAt());
        return dto;
    }

    private AdminPostDTO convertToAdminPostDTO(Post post) {
        AdminPostDTO dto = new AdminPostDTO();
        BeanUtils.copyProperties(post, dto);
        return dto;
    }
}