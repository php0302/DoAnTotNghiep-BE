package com.example.project_management.feature.attachment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation lưu file trên local disk của server.
 * Phù hợp cho môi trường đồ án / dev.
 * Production nên dùng AWS S3 hoặc Cloudinary.
 */
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String subDirectory) throws IOException {
        // Tạo tên file unique để tránh trùng lặp
        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex); // ví dụ: ".pdf"
        }
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extension;

        // Tạo thư mục nếu chưa tồn tại
        Path directory = Paths.get(uploadDir, subDirectory);
        Files.createDirectories(directory);

        // Lưu file
        Path destination = directory.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved: {}/{}", subDirectory, uniqueFileName);

        // Trả về URL tương đối để serve qua /uploads/**
        return "/uploads/" + subDirectory + "/" + uniqueFileName;
    }

    @Override
    public void delete(String fileUrl) throws IOException {
        // fileUrl = "/uploads/tasks/5/abc123.pdf"
        // → cắt "/uploads/" ở đầu, ghép với uploadDir
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            log.warn("Invalid fileUrl for deletion: {}", fileUrl);
            return;
        }
        String relativePath = fileUrl.substring("/uploads/".length()); // "tasks/5/abc123.pdf"
        Path filePath = Paths.get(uploadDir).resolve(relativePath);

        boolean deleted = Files.deleteIfExists(filePath);
        if (deleted) {
            log.info("File deleted: {}", filePath);
        } else {
            log.warn("File not found on disk (already deleted?): {}", filePath);
        }
    }
}
