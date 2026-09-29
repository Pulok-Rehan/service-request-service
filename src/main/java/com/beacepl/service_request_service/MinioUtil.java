package com.beacepl.service_request_service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioUtil {

    private static MinioClient minioClient;
    private static String bucketName;

    public MinioUtil(MinioClient client,
                     @Value("${minio.bucket:clientportal}") String bucket) {
        MinioUtil.minioClient = client;
        MinioUtil.bucketName = bucket;
    }

    public static String getImageUrl(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        if (objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return objectName;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(60 * 60) // 1 hour
                            .build()
            );
        } catch (Exception e) {
            return objectName;
        }
    }
}