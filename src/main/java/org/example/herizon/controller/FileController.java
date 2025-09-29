package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 提供文件上传功能，支持：
 * - 图片上传（头像、帖子图片）
 * - 视频上传（帖子视频）
 * - 文件格式验证和大小限制
 * <p>
 * 接口路径前缀：/api/files
 *
 * @author Kokoa
 */
@Tag(name = "文件上传", description = "图片、视频等文件上传API")
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

    /**
     * 上传图片
     * <p>
     * 支持jpg、png、gif格式，最大5MB
     *
     * @param file   图片文件
     * @param type   图片类型：avatar=头像，post=帖子图片
     * @param userId 用户ID，从请求头获取
     * @return 上传结果，包含文件URL
     */
    @Operation(summary = "上传图片", description = "上传头像或帖子图片，支持jpg、png、gif格式")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "图片类型") @RequestParam(defaultValue = "post") String type,
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        try {
            // 验证文件
            validateImageFile(file);

            // 生成文件名和路径
            String fileName = generateFileName(file.getOriginalFilename());
            String subDir = type + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filePath = UPLOAD_DIR + subDir + "/" + fileName;

            // 创建目录
            File uploadFile = new File(filePath);
            uploadFile.getParentFile().mkdirs();

            // 保存文件
            file.transferTo(uploadFile);

            // 返回文件URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/" + filePath);
            result.put("fileName", fileName);
            result.put("originalName", file.getOriginalFilename());

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频
     * <p>
     * 支持mp4、avi格式，最大100MB
     *
     * @param file   视频文件
     * @param userId 用户ID，从请求头获取
     * @return 上传结果，包含文件URL
     */
    @Operation(summary = "上传视频", description = "上传帖子视频，支持mp4、avi格式")
    @PostMapping("/video")
    public Result<Map<String, String>> uploadVideo(
            @Parameter(description = "视频文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        try {
            // 验证文件
            validateVideoFile(file);

            // 生成文件名和路径
            String fileName = generateFileName(file.getOriginalFilename());
            String subDir = "video/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filePath = UPLOAD_DIR + subDir + "/" + fileName;

            // 创建目录
            File uploadFile = new File(filePath);
            uploadFile.getParentFile().mkdirs();

            // 保存文件
            file.transferTo(uploadFile);

            // 返回文件URL
            Map<String, String> result = new HashMap<>();
            result.put("url", "/" + filePath);
            result.put("fileName", fileName);
            result.put("originalName", file.getOriginalFilename());

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(500, "上传失败：" + e.getMessage());
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("图片大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("只支持图片文件");
        }

        String[] allowedTypes = {"image/jpeg", "image/png", "image/gif"};
        boolean isValidType = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new RuntimeException("只支持jpg、png、gif格式");
        }
    }

    /**
     * 验证视频文件
     */
    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new RuntimeException("视频大小不能超过100MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new RuntimeException("只支持视频文件");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}