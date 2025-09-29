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
}