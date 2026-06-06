package com.example.project_management.feature.attachment;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction layer cho storage.
 * Hiện tại implement bằng Local Storage.
 * Sau này có thể swap sang AWS S3 chỉ cần tạo S3StorageService implements interface này.
 */
public interface StorageService {

    /**
     * Lưu file vào storage.
     *
     * @param file         file cần lưu
     * @param subDirectory thư mục con ví dụ: "tasks/5" hoặc "comments/12"
     * @return URL tương đối để truy cập file, ví dụ: "/uploads/tasks/5/uuid.pdf"
     */
    String store(MultipartFile file, String subDirectory) throws IOException;

    /**
     * Xóa file khỏi storage.
     *
     * @param fileUrl URL tương đối của file (giá trị trả về từ store())
     */
    void delete(String fileUrl) throws IOException;
}
