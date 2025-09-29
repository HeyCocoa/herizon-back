package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.PageResult;
import org.example.herizon.common.Result;
import org.example.herizon.dto.TagDTO;
import org.example.herizon.dto.CreateTagRequest;
import org.example.herizon.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 * <p>
 * 提供标签相关的REST API接口，包括：
 * - 分页查询标签列表
 * - 根据ID查询标签详情
 * - 创建新标签
 * - 更新标签信息
 * - 搜索标签
 * - 获取热门标签
 * <p>
 * 接口路径前缀：/api/tags
 *
 * @author Kokoa
 */
@Tag(name = "标签管理", description = "标签分类和主题管理API接口")
@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 分页查询标签列表
     * <p>
     * 支持按帖子数量排序，用于展示热门标签
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认20
     * @param sortBy  排序方式：count=按帖子数量降序，time=按创建时间降序，默认count
     * @return 分页的标签DTO列表
     */
    @Operation(summary = "分页查询标签列表", description = "支持按帖子数量或创建时间排序")
    @GetMapping
    public Result<PageResult<TagDTO>> getTagList(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "排序方式：count=按帖子数量，time=按创建时间") @RequestParam(defaultValue = "count") String sortBy) {

        PageResult<TagDTO> result = tagService.getTagList(current, size, sortBy);
        return Result.success(result);
    }

    /**
     * 根据ID查询标签详情
     * <p>
     * 获取标签的详细信息，包括使用该标签的帖子数量
     *
     * @param id 标签ID
     * @return 标签详情DTO
     */
    @Operation(summary = "查询标签详情", description = "根据ID查询标签的详细信息")
    @GetMapping("/{id}")
    public Result<TagDTO> getTagById(@Parameter(description = "标签ID") @PathVariable Long id) {
        TagDTO tag = tagService.getTagById(id);
        if (tag == null) {
            return Result.error(404, "标签不存在");
        }
        return Result.success(tag);
    }

    /**
     * 创建新标签
     * <p>
     * 根据系统变更要求，只有管理员可以创建新的标签分类
     * 普通用户发帖时只能从已有标签中选择
     *
     * @param request 创建标签请求，包含标签名称和描述
     * @param userId  创建者用户ID，从请求头获取
     * @return 创建的标签DTO
     */
    @Operation(summary = "创建新标签", description = "只有管理员可以创建新标签，用户发帖时只能选择已有标签")
    @PostMapping
    public Result<TagDTO> createTag(
            @Parameter(description = "创建标签请求") @RequestBody CreateTagRequest request,
            @Parameter(description = "创建者用户ID") @RequestHeader("userId") Long userId) {

        TagDTO tag = tagService.createTag(request, userId);
        return Result.success(tag);
    }

    /**
     * 更新标签信息
     * <p>
     * 管理员可以更新标签的名称和描述
     *
     * @param id      标签ID
     * @param request 更新请求，包含新的标签信息
     * @param userId  操作用户ID，从请求头获取
     * @return 更新后的标签DTO
     */
    @Operation(summary = "更新标签信息", description = "更新标签的名称和描述")
    @PutMapping("/{id}")
    public Result<TagDTO> updateTag(
            @Parameter(description = "标签ID") @PathVariable Long id,
            @Parameter(description = "更新请求") @RequestBody CreateTagRequest request,
            @Parameter(description = "操作用户ID") @RequestHeader("userId") Long userId) {

        TagDTO tag = tagService.updateTag(id, request);
        return Result.success(tag);
    }

    /**
     * 搜索标签
     * <p>
     * 根据关键词搜索标签名称，支持模糊匹配
     *
     * @param keyword 搜索关键词
     * @param limit   返回结果数量限制，默认10
     * @return 匹配的标签列表
     */
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签，支持模糊匹配")
    @GetMapping("/search")
    public Result<List<TagDTO>> searchTags(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回结果数量限制") @RequestParam(defaultValue = "10") Integer limit) {

        List<TagDTO> tags = tagService.searchTags(keyword, limit);
        return Result.success(tags);
    }

    /**
     * 获取热门标签
     * <p>
     * 获取按帖子数量排序的热门标签，用于首页展示
     *
     * @param limit 返回数量限制，默认10
     * @return 热门标签列表
     */
    @Operation(summary = "获取热门标签", description = "获取按帖子数量排序的热门标签")
    @GetMapping("/hot")
    public Result<List<TagDTO>> getHotTags(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {

        List<TagDTO> tags = tagService.getHotTags(limit);
        return Result.success(tags);
    }

    /**
     * 删除标签
     * <p>
     * 逻辑删除标签，只有管理员可以执行此操作
     * 删除标签时会同时删除相关的帖子-标签关联记录
     *
     * @param id     标签ID
     * @param userId 操作用户ID，从请求头获取
     * @return 删除结果
     */
    @Operation(summary = "删除标签", description = "逻辑删除标签，只有管理员可以执行")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID") @PathVariable Long id,
            @Parameter(description = "操作用户ID") @RequestHeader("userId") Long userId) {

        tagService.deleteTag(id, userId);
        return Result.success();
    }
}