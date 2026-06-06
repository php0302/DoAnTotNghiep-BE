package com.example.project_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình serve static files từ thư mục uploads.
 * Cho phép truy cập file qua URL: GET /uploads/tasks/5/uuid.pdf
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đảm bảo uploadDir có trailing slash và dùng "file:" prefix cho absolute path
        String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
        // Nếu là relative path thì cần resolve về absolute
        if (!location.startsWith("/") && !location.matches("[A-Za-z]:.*")) {
            // Relative path — Spring sẽ resolve từ working directory
            location = "file:" + location;
        } else {
            location = "file:" + location;
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600); // Cache 1 giờ
    }
}
