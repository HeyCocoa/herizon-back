package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.FollowResponseDTO;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.UserMapper;
import org.example.herizon.service.ActionService;
import org.example.herizon.service.FollowService;
import org.example.herizon.dto.FollowUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户行为管理
 * <p>
 * 提供统一的用户行为操作API，包括：
 * - 点赞/取消点赞
 * - 收藏/取消收藏
 * - 举报
 * <p>
 * 接口路径前缀：/api/actions
 *
 * @author Kokoa
 */
@Tag (name = "用户行为管理", description = "点赞、收藏、分享、举报等用户行为API")
@RestController
@RequestMapping ("/actions")
public class ActionController{

    @Autowired
    private ActionService actionService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowService followService;


    /**
     * 点赞或取消点赞
     *
     * @param targetId   目标对象ID（帖子或评论）
     * @param targetType 目标对象类型，默认post
     * @param userId     操作用户ID，从请求头获取
     * @return 当前点赞状态 true=已点赞 false=已取消
     */
    @Operation (summary = "切换点赞状态", description = "对帖子或评论执行点赞/取消点赞")
    @PostMapping ("/like")
    public Result<Boolean> toggleLike(
        @Parameter (description = "目标对象ID") @RequestParam Long targetId,
        @Parameter (description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
        @Parameter (description = "操作用户ID") @RequestHeader ("userId") Long userId){

        if( userId==null ){
            return Result.error("用户未登录");
        }
        if( targetId==null ){
            return Result.error("目标ID不能为空");
        }

        String normalizedTargetType = (targetType==null || targetType.trim().isEmpty())
            ? "post"
            : targetType.trim().toLowerCase();

        boolean liked = actionService.toggleLike(userId, targetId, normalizedTargetType);
        return Result.success(liked);
    }

    /**
     * 收藏或取消收藏
     *
     * @param targetId   目标对象ID（帖子或其他可收藏对象）
     * @param targetType 目标对象类型，默认post
     * @param userId     操作用户ID，从请求头获取
     * @return 当前收藏状态 true=已收藏 false=已取消
     */
    @Operation (summary = "切换收藏状态", description = "对帖子执行收藏/取消收藏")
    @PostMapping ("/collect")
    public Result<Boolean> toggleCollect(
        @Parameter (description = "目标对象ID") @RequestParam Long targetId,
        @Parameter (description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
        @Parameter (description = "操作用户ID") @RequestHeader ("userId") Long userId){

        if( userId==null ){
            return Result.error("用户未登录");
        }
        if( targetId==null ){
            return Result.error("目标ID不能为空");
        }

        String normalizedTargetType = (targetType==null || targetType.trim().isEmpty())
            ? "post"
            : targetType.trim().toLowerCase();

        boolean collected = actionService.toggleCollect(userId, targetId, normalizedTargetType);
        return Result.success(collected);
    }

    /**
     * 举报内容
     * <p>
     * 用户举报不当内容，记录举报行为和举报原因
     *
     * @param targetId   目标对象ID（如帖子ID、评论ID）
     * @param targetType 目标对象类型，默认"post"
     * @param reason     举报原因
     * @param userId     操作用户ID，从请求头获取
     * @return 成功响应
     */
    @Operation (summary = "举报内容", description = "举报不当内容，记录举报原因")
    @PostMapping ("/report")
    public Result<Void> report(
        @Parameter (description = "目标对象ID") @RequestParam Long targetId,
        @Parameter (description = "目标对象类型") @RequestParam (defaultValue = "post") String targetType,
        @Parameter (description = "举报原因") @RequestParam String reason,
        @Parameter (description = "操作用户ID") @RequestHeader ("userId") Long userId){

        actionService.report(userId, targetId, targetType, reason);
        return Result.success();
    }

    /**
     * 切换关注状态
     * <p>
     * 如果用户已关注目标用户则取消关注，未关注则添加关注
     * 返回关注状态和目标用户的粉丝数
     *
     * @param targetUserId 目标用户ID
     * @param userId       操作用户ID，从请求头获取
     * @return 关注状态和粉丝数
     */
    @Operation (summary = "切换关注状态", description = "如果已关注则取消，未关注则添加")
    @PostMapping ("/follow")
    public Result<FollowResponseDTO> toggleFollow(
        @Parameter (description = "目标用户ID") @RequestParam Long targetUserId,
        @Parameter (description = "操作用户ID") @RequestHeader ("userId") Long userId){

        // 输入验证
        if( userId==null || targetUserId==null ){
            return Result.error("参数不能为空");
        }

        if( userId.equals(targetUserId) ){
            return Result.error("不能关注自己");
        }

        // 验证目标用户是否存在
        User targetUser = userMapper.selectOneById(targetUserId);
        if( targetUser==null ){
            return Result.error("目标用户不存在");
        }

        // 切换关注状态
        boolean isFollowing = followService.toggleFollow(userId, targetUserId);

        // ��ѯĿ���û��ķ�˿��
        Long followersCount = followService.countFollowers(targetUserId);

        // ������Ӧ
        FollowResponseDTO response = new FollowResponseDTO();
        response.setIsFollowing(isFollowing);
        response.setFollowersCount(followersCount);

        return Result.success(response);
    }

    /**
     * 获取关注列表
     * <p>
     * 查询指定用户关注的所有用户，支持分页
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页数量，默认20
     * @return 分页的用户资料列表
     */
    @Operation (summary = "获取关注列表", description = "查询用户关注的所有用户")
    @GetMapping ("/following")
    public Result<PageResult<FollowUserDTO>> getFollowing(
        @Parameter (description = "用户ID") @RequestParam Long userId,
        @Parameter (description = "当前页码") @RequestParam (defaultValue = "1") Integer current,
        @Parameter (description = "每页数量") @RequestParam (defaultValue = "20") Integer size,
        @Parameter (description = "��ǰ�û�ID") @RequestHeader (value = "userId", required = false) Long currentUserId){

        // 输入验证
        if( userId==null ){
            return Result.error("用户ID不能为空");
        }

        if( current==null || current < 1 ){
            current = 1;
        }

        if( size==null || size < 1 || size > 100 ){
            size = 20;
        }

        // 查询关注列表
        PageResult<FollowUserDTO> followingPage = followService.getFollowing(userId, currentUserId, current, size);

        return Result.success(followingPage);
    }

    /**
     * 获取粉丝列表
     * <p>
     * 查询关注指定用户的所有用户，支持分页
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页数量，默认20
     * @return 分页的用户资料列表
     */
    @Operation (summary = "获取粉丝列表", description = "查询关注该用户的所有用户")
    @GetMapping ("/followers")
    public Result<PageResult<FollowUserDTO>> getFollowers(
        @Parameter (description = "用户ID") @RequestParam Long userId,
        @Parameter (description = "当前页码") @RequestParam (defaultValue = "1") Integer current,
        @Parameter (description = "每页数量") @RequestParam (defaultValue = "20") Integer size,
        @Parameter (description = "��ǰ�û�ID") @RequestHeader (value = "userId", required = false) Long currentUserId){

        // 输入验证
        if( userId==null ){
            return Result.error("用户ID不能为空");
        }

        if( current==null || current < 1 ){
            current = 1;
        }

        if( size==null || size < 1 || size > 100 ){
            size = 20;
        }

        // 查询粉丝列表
        PageResult<FollowUserDTO> followersPage = followService.getFollowers(userId, currentUserId, current, size);

        return Result.success(followersPage);
    }

    /**
     * 获取收藏列表
     * <p>
     * 查询用户收藏的所有帖子，支持分页
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页数量，默认20
     * @return 分页的帖子列表
     */
    @Operation (summary = "获取收藏列表", description = "查询用户收藏的所有帖子")
    @GetMapping ("/collections")
    public Result<PageResult<org.example.herizon.entity.Post>> getCollections(
        @Parameter (description = "用户ID") @RequestParam Long userId,
        @Parameter (description = "当前页码") @RequestParam (defaultValue = "1") Integer current,
        @Parameter (description = "每页数量") @RequestParam (defaultValue = "20") Integer size){

        // 输入验证
        if( userId==null ){
            return Result.error("用户ID不能为空");
        }

        if( current==null || current < 1 ){
            current = 1;
        }

        if( size==null || size < 1 || size > 100 ){
            size = 20;
        }

        // 查询收藏列表
        PageResult<org.example.herizon.entity.Post> collectionsPage = actionService.getCollections(userId, current, size);

        return Result.success(collectionsPage);
    }
}
