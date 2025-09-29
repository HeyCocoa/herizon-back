package org.example.herizon.service;

import com.mybatisflex.core.query.QueryWrapper;
import org.example.herizon.entity.Post;
import org.example.herizon.entity.UserAction;
import org.example.herizon.mapper.PostMapper;
import org.example.herizon.mapper.UserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户行为管理服务类
 * <p>
 * 提供统一的用户行为操作功能：
 * - 点赞/取消点赞切换
 * - 收藏/取消收藏切换
 * - 分享记录
 * - 举报记录
 * <p>
 * 所有操作都会同步更新相关对象的计数字段，
 * 并通过逻辑删除实现取消操作
 *
 * @author Kokoa
 */
@Service
public class ActionService{

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 切换点赞状态
     *
     * @param userId     用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     * @return true=已点赞，false=已取消点赞
     */
    @Transactional
    public boolean toggleLike(Long userId, Long targetId, String targetType){
        return toggleAction(userId, targetId, targetType, 0, "likeCount");
    }

    /**
     * 切换收藏状态
     *
     * @param userId     用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     * @return true=已收藏，false=已取消收藏
     */
    @Transactional
    public boolean toggleCollect(Long userId, Long targetId, String targetType){
        return toggleAction(userId, targetId, targetType, 1, "collectCount");
    }

    /**
     * 记录分享行为
     * <p>
     * 分享行为不支持取消，每次分享都会增加分享计数
     *
     * @param userId     用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     */
    @Transactional
    public void share(Long userId, Long targetId, String targetType){
        // 创建分享记录
        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setTargetId(targetId);
        action.setTargetType(targetType);
        action.setActionType(2);
        action.setCreatedAt(LocalDateTime.now());
        userActionMapper.insert(action);

        // 同步更新目标对象的分享计数
        if( "post".equals(targetType) ){
            Post post = postMapper.selectOneById(targetId);
            if( post!=null ){
                post.setShareCount(post.getShareCount() + 1);
                postMapper.update(post);
            }
        }
    }

    /**
     * 记录举报行为
     *
     * @param userId     举报用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     * @param reason     举报原因
     */
    @Transactional
    public void report(Long userId, Long targetId, String targetType, String reason){
        UserAction action = new UserAction();
        action.setUserId(userId);
        action.setTargetId(targetId);
        action.setTargetType(targetType);
        action.setActionType(3);
        action.setExtraData("{\"reason\":\"" + reason + "\"}");
        action.setCreatedAt(LocalDateTime.now());
        userActionMapper.insert(action);
    }

    /**
     * 切换用户行为的通用方法
     * <p>
     * 如果用户已执行该行为则取消（逻辑删除），否则新增行为记录
     * 同时更新目标对象的相应计数字段
     *
     * @param userId     用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标对象类型
     * @param actionType 行为类型：0=点赞，1=收藏
     * @param countField 对应的计数字段名
     * @return true=新增行为，false=取消行为
     */
    private boolean toggleAction(Long userId, Long targetId, String targetType, Integer actionType, String countField){
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("user_id = ?", userId)
                .and("target_id = ?", targetId)
                .and("target_type = ?", targetType)
                .and("action_type = ?", actionType)
                .and("deleted = 0");

        UserAction existingAction = userActionMapper.selectOneByQuery(queryWrapper);

        boolean isAdded;
        if( existingAction!=null ){
            // 用户已执行过该行为，通过逻辑删除来取消
            existingAction.setDeleted(1);
            userActionMapper.update(existingAction);
            isAdded = false;
        }
        else{
            // 用户未执行过该行为，新增行为记录
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setTargetId(targetId);
            action.setTargetType(targetType);
            action.setActionType(actionType);
            action.setCreatedAt(LocalDateTime.now());
            userActionMapper.insert(action);
            isAdded = true;
        }

        // 同步更新目标对象的对应计数字段
        if( "post".equals(targetType) ){
            Post post = postMapper.selectOneById(targetId);
            if( post!=null ){
                if( "likeCount".equals(countField) ){
                    post.setLikeCount(post.getLikeCount() + (isAdded ? 1 : -1));
                }
                else if( "collectCount".equals(countField) ){
                    post.setCollectCount(post.getCollectCount() + (isAdded ? 1 : -1));
                }
                postMapper.update(post);
            }
        }

        return isAdded;
    }
}