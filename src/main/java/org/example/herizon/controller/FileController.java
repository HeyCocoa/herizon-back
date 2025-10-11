package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.herizon.common.Result;
import org.example.herizon.config.FileStorageProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * �ļ��ϴ�������
 * <p>
 * �ṩ�ļ��ϴ����ܣ�֧�֣�
 * - ͼƬ�ϴ���ͷ������ͼƬ��
 * - �ļ���ʽ��֤�ʹ�С����
 * <p>
 * �ӿ�·��ǰ׺��/api/files
 *
 * @author Kokoa
 */
@Tag(name = "�ļ��ϴ�", description = "ͼƬ�ļ��ϴ�API")
@RestController
@RequestMapping("/files")
public class FileController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageProperties fileStorageProperties;

    public FileController(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    /**
     * �ϴ�ͼƬ
     * <p>
     * ֧��jpg��png��gif��ʽ�����5MB
     *
     * @param file   ͼƬ�ļ�
     * @param type   ͼƬ���ͣ�avatar=ͷ��post=����ͼƬ
     * @param userId �û�ID��������ͷ��ȡ
     * @return �ϴ�����������ļ�URL
     */
    @Operation(summary = "�ϴ�ͼƬ", description = "�ϴ�ͷ�������ͼƬ��֧��jpg��png��gif��ʽ")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @Parameter(description = "ͼƬ�ļ�") @RequestParam("file") MultipartFile file,
            @Parameter(description = "ͼƬ����") @RequestParam(defaultValue = "post") String type,
            @Parameter(description = "�û�ID") @RequestHeader("userId") Long userId,
            HttpServletRequest request) {

        try {
            // ��֤�ļ�
            validateImageFile(file);

            // ����ͼƬ���ͺ͵����ļ��洢·��
            String sanitizedType = sanitizeType(type);
            String fileName = generateFileName(file.getOriginalFilename());
            LocalDate today = LocalDate.now();
            String[] dateSegments = today.format(DATE_PATH_FORMATTER).split("/");
            Path relativeDir = Paths.get(sanitizedType, dateSegments);
            Path targetDir = fileStorageProperties.getUploadDir().resolve(relativeDir);
            Files.createDirectories(targetDir);

            // �����ļ�
            Path targetFile = targetDir.resolve(fileName);
            file.transferTo(targetFile.toFile());

            // �����ļ�URL
            String datePath = String.join("/", dateSegments);
            String basePath = "/uploads/" + sanitizedType + "/" + datePath + "/" + fileName;
            String contextPath = request.getContextPath();
            String relativeUrl = (contextPath == null ? "" : contextPath) + basePath;
            if (!relativeUrl.startsWith("/")) {
                relativeUrl = "/" + relativeUrl;
            }
            String absoluteUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(basePath)
                    .toUriString();
            Map<String, String> result = new HashMap<>();
            result.put("url", absoluteUrl);
            result.put("relativePath", relativeUrl);
            result.put("fileName", fileName);
            result.put("originalName", file.getOriginalFilename());

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(500, "�ϴ�ʧ�ܣ�" + e.getMessage());
        }
    }


    /**
     * ��֤ͼƬ�ļ�
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("�ļ�����Ϊ��");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("ͼƬ��С���ܳ���5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("ֻ֧��ͼƬ�ļ�");
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
            throw new RuntimeException("ֻ֧��jpg��png��gif��ʽ");
        }
    }

    /**
     * ����Ψһ�ļ���
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * ͼƬ������֤���̶�������ȥ����ȷ��·���ڰ�ȫ��չ
     */
    private String sanitizeType(String type) {
        if (type == null || type.isBlank()) {
            return "post";
        }
        String normalized = type.trim().toLowerCase();
        if (!normalized.matches("^[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("ͼƬ�������Ϸ�");
        }
        return normalized;
    }
}
