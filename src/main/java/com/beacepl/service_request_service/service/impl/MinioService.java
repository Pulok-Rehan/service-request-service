package com.beacepl.service_request_service.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket:clientportal}")
    private String bucket;

    @Value("${minio.endpoint:http://localhost:7000}")
    private String endpoint;

    public String upload(
            MultipartFile file,
            String investorCode,
            String serviceRequestId,
            String fieldName
    ) throws Exception {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file.png";
        }
        originalFilename = originalFilename.replaceAll("\\s+", "_");

        String objectName = String.format("%s-%s-%s-%s",
                investorCode != null ? investorCode : "INV",
                serviceRequestId != null ? serviceRequestId : UUID.randomUUID().toString().substring(0, 8),
                fieldName != null ? fieldName : "file",
                originalFilename
        );

        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucket)
                        .build()
        );

        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            .build()
            );
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(
                                file.getInputStream(),
                                file.getSize(),
                                -1
                        )
                        .contentType(file.getContentType())
                        .build()
        );

        log.info("File uploaded successfully to MinIO object: {}", objectName);
        return objectName;
    }

    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        // If it's already a full HTTP URL, return as is
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(60 * 60) // 1 hour
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for object {}: {}", objectName, e.getMessage());
            return objectName;
        }
    }

    public void removeObject(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("Successfully removed object from MinIO: {}", objectName);
        } catch (Exception e) {
            log.warn("Failed to delete object {} from MinIO: {}", objectName, e.getMessage());
        }
    }
}