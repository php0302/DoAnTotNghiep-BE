package com.example.project_management.feature.attachment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String store(MultipartFile file, String subDirectory) throws IOException {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String s3Key = subDirectory + "/" + uniqueFileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Trả về url đầy đủ của file trên S3
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
            log.info("Uploaded successfully to S3: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("S3 Upload error: {}", e.getMessage(), e);
            throw new IOException("Không thể upload file lên S3: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            log.warn("Invalid S3 URL for deletion: {}", fileUrl);
            return;
        }

        // Tách lấy s3Key từ S3 URL: https://bucket.s3.region.amazonaws.com/s3Key
        String marker = ".amazonaws.com/";
        int markerIndex = fileUrl.indexOf(marker);
        String s3Key = fileUrl.substring(markerIndex + marker.length());

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted from S3: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 deletion error: {}", e.getMessage(), e);
            throw new IOException("Không thể xóa file trên S3: " + e.getMessage(), e);
        }
    }
}
