package org.example.herizon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Exposes uploaded files via /uploads/** endpoints.
 */
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    public FileResourceConfig(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileStorageProperties.getUploadDirResourceLocation());
    }
}
