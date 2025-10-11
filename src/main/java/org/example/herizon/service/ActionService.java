package org.example.herizon.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.common.PageResult;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.UserAction;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户行为服务
 * <p>
 * 提供点赞、收藏、举报等通用操作能力。
 */
@Service
public class ActionService {

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 切换点赞状态。
     */
    @Transactional
    public boolean toggleLike(Long userId, Long targetId, String targetType) {
        return toggleAction(userId, targetId, targetType, 0);
    }

    /**
     * 切换收藏状态。
     */
    @Transactional
    public boolean toggleCollect(Long userId, Long targetId, String targetType) {
        return toggleAction(userId, targetId, targetType, 1);
    }

    /**
     * 记录举报。
     */
    @Transactional
    public void report(Long userId, Long targetId, String targetType, String reason) {
        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setTargetId(targetId);
        action.setTargetType(targetType);
        action.setActionType(3);
        action.setExtraData("{\"reason\":\"" + reason + "\"}");
        action.setCreatedAt(LocalDateTime.now());
        action.setDeleted(0);
        userActionMapper.insert(action);
    }

    private boolean toggleAction(Long userId,
                                 Long targetId,
                                 String targetType,
                                 Integer actionType) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .where("user_id = ?", userId)
            .and("target_id = ?", targetId)
            .and("target_type = ?", targetType)
            .and("action_type = ?", actionType)
            .and("(deleted = 0 OR deleted IS NULL)");

        UserAction existingAction = userActionMapper.selectOneByQuery(queryWrapper);

        if (existingAction != null) {
            existingAction.setDeleted(1);
            userActionMapper.update(existingAction);
            return false;
        }

        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setTargetId(targetId);
        action.setTargetType(targetType);
        action.setActionType(actionType);
        action.setCreatedAt(LocalDateTime.now());
        action.setDeleted(0);
        userActionMapper.insert(action);
        return true;
    }

    /**
     * 获取收藏的帖子列表。
     */
    public PageResult<Post> getCollections(Long userId, Integer current, Integer size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
            .select("posts.*, users.nickname AS authorNickname, users.username AS authorUsername")
            .from("posts")
            .leftJoin("users").on("users.id = posts.user_id")
            .where("EXISTS (SELECT 1 FROM user_actions WHERE user_actions.target_id = posts.id " +
                   "AND user_actions.user_id = ? AND user_actions.action_type = 1 " +
                   "AND (user_actions.deleted = 0 OR user_actions.deleted IS NULL) AND user_actions.target_type = 'post')", userId)
            .and("posts.deleted = 0")
            .orderBy("posts.created_at DESC");

        Page<Post> page = Page.of(current, size);
        Page<Post> postPage = postMapper.paginate(page, queryWrapper);
        return PageResult.of(postPage.getRecords(), postPage.getTotalRow(), (long) current, (long) size);
    }
}
