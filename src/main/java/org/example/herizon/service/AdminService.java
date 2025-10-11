package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.dto.AdminUserDTO;
import org.example.herizon.dto.PostDTO;
import org.example.herizon.entity.User;
import org.example.herizon.entity.Post;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.TagMapper;
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
 * 管理员服务类（简化版 - 符合MVP原则）
 * <p>
 * 简化说明（2025-10-02）：
 * 删除了过度设计的功能（举报处理、帖子管理、用户管理等），
 * 仅保留核心的用户审核和统计功能
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

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostService postService;

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
     * 管理员将用户提升为管理员角色
     *
     * @param targetUserId 目标用户ID
     * @param adminId      操作管理员ID
     */
    public void promoteUserToAdmin(Long targetUserId, Long adminId) {
        validateAdminPermission(adminId);

        if (targetUserId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        User targetUser = userMapper.selectOneById(targetUserId);
        if (targetUser == null || (targetUser.getDeleted() != null && targetUser.getDeleted() == 1)) {
            throw new RuntimeException("用户不存在或已被删除");
        }

        if (targetUser.getRole() != null && targetUser.getRole() == 2) {
            return;
        }

        targetUser.setRole(2);
        targetUser.setUpdatedAt(LocalDateTime.now());
        userMapper.update(targetUser);
    }

    /**
     * 管理员删除用户（逻辑删除）
     *
     * @param targetUserId 目标用户ID
     * @param adminId      操作管理员ID
     */
    @Transactional
    public void deleteUser(Long targetUserId, Long adminId) {
        validateAdminPermission(adminId);

        if (targetUserId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (targetUserId.equals(adminId)) {
            throw new RuntimeException("不能删除自己的账号");
        }

        User targetUser = userMapper.selectOneById(targetUserId);
        if (targetUser == null || (targetUser.getDeleted() != null && targetUser.getDeleted() == 1)) {
            throw new RuntimeException("用户不存在或已被删除");
        }

        targetUser.setDeleted(1);
        targetUser.setUpdatedAt(LocalDateTime.now());
        userMapper.update(targetUser);

        QueryWrapper postQuery = QueryWrapper.create()
                .where("user_id = ?", targetUserId)
                .and("deleted = 0");
        List<Post> posts = postMapper.selectListByQuery(postQuery);
        for (Post post : posts) {
            postService.deletePost(post.getId(), adminId);
        }
    }

    /**
     * 管理员分页查询所有用户
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param adminId 管理员ID
     * @return 分页的用户列表
     */
    public PageResult<AdminUserDTO> getAllUsers(Integer current, Integer size, Long adminId) {
        validateAdminPermission(adminId);

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("deleted = 0")
                .orderBy("created_at DESC");

        Page<User> page = userMapper.paginate(Page.of(current, size), queryWrapper);
        List<AdminUserDTO> users = page.getRecords().stream()
                .map(this::convertToAdminUserDTO)
                .collect(Collectors.toList());

        return PageResult.of(users, page.getTotalRow(), (long) current, (long) size);
    }

    /**
     * 管理员分页查询所有帖子
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param adminId 管理员ID
     * @return 分页的帖子列表
     */
    public PageResult<PostDTO> getAllPosts(Integer current, Integer size, Long adminId) {
        validateAdminPermission(adminId);
        return postService.getAllPostsForAdmin(current, size);
    }

    /**
     * 管理员删除帖子
     *
     * @param postId  帖子ID
     * @param adminId 管理员ID
     */
    public void deletePost(Long postId, Long adminId) {
        validateAdminPermission(adminId);
        postService.deletePost(postId, adminId);
    }

    /**
     * 获取平台统计数据（简化版 - 符合MVP原则）
     * <p>
     * 简化说明（2025-10-02）：
     * 删除了复杂的统计逻辑，仅保留3个核心统计指标，
     * 符合最小可行产品原则
     *
     * @param adminId 管理员ID
     * @return 基础统计数据（pendingUsers, totalTags, totalPosts）
     */
    public Object getStatistics(Long adminId) {
        validateAdminPermission(adminId);

        Map<String, Object> stats = new HashMap<>();

        // 1. 待审核用户数（role=0 且有问卷数据）
        long pendingUsers = userMapper.selectCountByQuery(
                QueryWrapper.create()
                        .where("role = 0")
                        .and("questionnaire_data IS NOT NULL")
                        .and("deleted = 0")
        );

        // 2. 标签总数
        long totalTags = tagMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0")
        );

        // 3. 帖子总数
        long totalPosts = postMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0")
        );

        stats.put("pendingUsers", pendingUsers);
        stats.put("totalTags", totalTags);
        stats.put("totalPosts", totalPosts);

        return stats;
    }

    private AdminUserDTO convertToAdminUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setIsPendingVerification(user.getRole() == 0 && user.getQuestionnaireData() != null);

        Integer role = user.getRole();
        if (role != null) {
            String roleDescription;
            switch (role) {
                case 2 -> roleDescription = "管理员";
                case 1 -> roleDescription = "正式用户";
                default -> roleDescription = "体验用户";
            }
            dto.setRoleDescription(roleDescription);
        }
        return dto;
    }

}
