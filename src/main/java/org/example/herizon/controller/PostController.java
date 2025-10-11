package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.CreatePostRequest;
import org.example.herizon.dto.PostDTO;
import org.example.herizon.entity.Post;
import org.example.herizon.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子管理
 * <p>
 * 提供帖子相关的REST API接口，包括：
 * - 分页查询帖子列表
 * - 根据ID查询帖子详情
 * - 创建新帖子
 * - 增加浏览量
 * <p>
 * 接口路径前缀：/api/posts
 *
 * @author Kokoa
 */
@Tag(name = "帖子管理", description = "帖子相关的API接口")
@RestController
@RequestMapping ("/posts")
public class PostController{

    @Autowired
    private PostService postService;

    /**
     * 搜索帖子（模糊匹配）
     * <p>
     * 根据关键词搜索帖子，支持对标题和内容的模糊匹配
     * 搜索结果按时间倒序排列（最新的在前）
     * <p>
     * 技术实现路径：
     * 1. Controller层：接收搜索参数（keyword, current, size）
     * 2. Service层：PostService.searchPosts() 执行模糊查询
     * 3. Mapper层：使用 MyBatis-Flex QueryWrapper 构建 LIKE 查询
     * 4. 返回：PageResult<PostDTO> 包含完整帖子信息和分页元数据
     *
     * @param keyword 搜索关键词（必需参数）
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @return 分页的帖子DTO列表，按创建时间倒序
     */
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子，支持标题和内容的模糊匹配，结果按时间倒序")
    @GetMapping("/search")
    public Result<PageResult<PostDTO>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        // 参数校验：关键词不能为空
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }

        // 调用Service层执行搜索
        // 代码路径：PostService.searchPosts() -> PostMapper.paginate() -> MySQL LIKE查询
        PageResult<PostDTO> result = postService.searchPosts(keyword, current, size);
        return Result.success(result);
    }

    /**
     * 首页帖子列表（系统变更后简化版）
     * <p>
     * 根据系统变更要求，首页仅展示全部帖子，移除复杂筛选条件
     * 只保留推荐算法排序，不再支持标签筛选和排序选项
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @return 分页的帖子DTO列表，按推荐算法排序
     */
    @Operation(summary = "首页帖子列表", description = "首页展示全部帖子，按推荐算法排序")
    @GetMapping
    public Result<PageResult<PostDTO>> getHomePostList(
            @Parameter(description = "当前页码") @RequestParam (defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam (defaultValue = "10") Integer size){

        PageResult<PostDTO> result = postService.getHomePostList(current, size);
        return Result.success(result);
    }

    /**
     * 话题页面 - 根据标签筛选帖子
     * <p>
     * 为话题页面提供的API，显示指定标签下的所有帖子
     *
     * @param tagId   标签ID，必需参数
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @return 分页的帖子DTO列表
     */
    @Operation(summary = "根据标签查询帖子", description = "话题页面专用，显示指定标签下的所有帖子")
    @GetMapping("/by-tag/{tagId}")
    public Result<PageResult<PostDTO>> getPostsByTag(
            @Parameter(description = "标签ID") @PathVariable Long tagId,
            @Parameter(description = "当前页码") @RequestParam (defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam (defaultValue = "10") Integer size){

        PageResult<PostDTO> result = postService.getPostsByTag(tagId, current, size);
        return Result.success(result);
    }

    /**
     * 统计指定标签下的帖子数量
     * <p>
     * 实时查询指定标签下的有效帖子总数，用于话题页面右侧显示
     * 不使用缓存的post_count字段，确保数据准确性
     *
     * @param tagId 标签ID
     * @return 该标签下的帖子数量
     */
    @Operation(summary = "统计标签帖子数量", description = "实时统计指定标签下的帖子总数")
    @GetMapping("/by-tag/{tagId}/count")
    public Result<Long> getPostCountByTag(
            @Parameter(description = "标签ID") @PathVariable Long tagId) {

        if (tagId == null) {
            return Result.error("标签ID不能为空");
        }

        Long count = postService.getPostCountByTag(tagId);
        return Result.success(count);
    }

    /**
     * 根据ID查询帖子详情
     * <p>
     * 查询帖子详情时会自动增加浏览量，如果提供了当前用户ID，
     * 还会返回用户对该帖子的操作状态（是否点赞、收藏等）
     *
     * @param id            帖子ID
     * @param currentUserId 当前用户ID，可选，用于查询用户操作状态
     * @return 帖子详情DTO
     */
    @Operation(summary = "查询帖子详情", description = "根据ID查询帖子详情，自动增加浏览量")
    @GetMapping ("/{id}")
    public Result<PostDTO> getPostById(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "当前用户ID") @RequestHeader (value = "userId", required = false) Long currentUserId){

        PostDTO post = postService.getPostById(id, currentUserId);
        if( post==null ){
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 创建新帖子
     *
     * @param request 创建帖子请求，包含标题、内容、类型、标签等
     * @param userId  创建者用户ID，从请求头获取
     * @return 创建的帖子实体
     */
    @Operation(summary = "创建新帖子", description = "创建新帖子，支持添加标签")
    @PostMapping
    public Result<Post> createPost(
            @Parameter(description = "创建帖子请求") @RequestBody CreatePostRequest request,
            @Parameter(description = "创建者用户ID") @RequestHeader ("userId") Long userId){

        Post post = postService.createPost(request, userId);
        return Result.success(post);
    }

    /**
     * 增加帖子浏览量
     * <p>
     * 注意：浏览量增加已经在getPostById方法中自动处理，
     * 这个接口主要用于兼容客户端的显式调用
     *
     * @param id 帖子ID
     * @return 成功响应
     */
    @Operation(summary = "增加帖子浏览量", description = "手动增加帖子浏览量")
    @PostMapping ("/{id}/view")
    public Result<Void> increaseViewCount(@Parameter(description = "帖子ID") @PathVariable Long id){
        // 浏览量增加已在getPostById中处理
        return Result.success();
    }

    /**
     * 对投票帖进行投票
     * <p>
     * 业务规则：
     * - 只能对投票帖（post_type=1）进行投票
     * - 每个用户每个投票帖只能投一票
     * - 可以切换投票选项（撤回原投票，投新选项）
     * - 不能重复投同一选项
     *
     * @param id       投票帖ID
     * @param requestBody 请求体，包含optionId投票选项ID
     * @param userId   投票用户ID，从请求头获取
     * @return 成功响应或错误信息
     */
    @Operation(summary = "对投票帖进行投票", description = "用户对投票帖的某个选项进行投票，支持切换选项")
    @PostMapping("/{id}/vote")
    public Result<Void> vote(
            @Parameter(description = "投票帖ID") @PathVariable Long id,
            @Parameter(description = "包含optionId的请求体") @RequestBody java.util.Map<String, Long> requestBody,
            @Parameter(description = "投票用户ID") @RequestHeader("userId") Long userId) {

        try {
            // 从请求体中提取optionId（前端通过POST请求体传递）
            Long optionId = requestBody.get("optionId");
            if (optionId == null) {
                return Result.error(400, "投票选项ID不能为空");
            }
            postService.vote(id, optionId, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 查询用户在某个投票帖中的投票选择
     * <p>
     * 用于前端显示用户已选择的投票选项，实现投票状态同步
     *
     * @param id     投票帖ID
     * @param userId 用户ID，从请求头获取
     * @return 用户选择的投票选项ID，如果未投票返回null
     */
    @Operation(summary = "查询我的投票选择", description = "查询用户在指定投票帖中选择的投票选项")
    @GetMapping("/{id}/my-vote")
    public Result<Long> getMyVote(
            @Parameter(description = "投票帖ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        Long optionId = postService.getMyVote(id, userId);
        return Result.success(optionId);
    }

    /**
     * 查询指定用户的帖子列表
     * <p>
     * 用于"我的帖子"页面，返回指定用户发布的所有帖子
     * 支持分页查询，按创建时间降序排列
     *
     * @param userId  用户ID
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @return 分页的帖子DTO列表
     */
    @Operation(summary = "查询用户帖子列表", description = "获取指定用户发布的所有帖子，用于我的帖子页面")
    @GetMapping("/user/{userId}")
    public Result<PageResult<PostDTO>> getUserPosts(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {

        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        PageResult<PostDTO> result = postService.getUserPosts(userId, current, size);
        return Result.success(result);
    }

    /**
     * 删除帖子
     * <p>
     * 仅允许帖子作者或管理员执行删除操作，底层采用逻辑删除。
     *
     * @param id     帖子ID
     * @param userId 操作用户ID（从请求头获取）
     * @return 删除结果
     */
    @Operation(summary = "删除帖子", description = "仅帖子作者或管理员可以删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable Long id,
            @Parameter(description = "操作用户ID") @RequestHeader("userId") Long userId) {

        postService.deletePost(id, userId);
        return Result.success();
    }
}
