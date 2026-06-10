package com.rms.admin.service.storage;

import com.rms.admin.config.StorageProperties;
import com.rms.admin.exception.ImageUploadException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        String bucket = storageProperties.getBucket();
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build());
            log.info("Bucket {} already exists", bucket);
        } catch (NoSuchBucketException e) {
            log.info("Bucket {} does not exist, creating it", bucket);
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucket)
                    .build());
            log.info("Bucket {} created successfully", bucket);
        } catch (Exception e) {
            log.error("Error checking/creating bucket {}: {}", bucket, e.getMessage());
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String objectKey) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(objectKey)
                    .contentType(file.getContentType() != null ? file.getContentType() : "image/jpeg")
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("Failed to upload file for key {}: {}", objectKey, e.getMessage());
            throw new ImageUploadException("Failed to upload property image", e);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.warn("Failed to delete object key {}: {}", objectKey, e.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey, int expiryMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toExternalForm();
    }

    public static String buildUserProfileImageKey(Long userId) {
        return String.format("users/%d/%s", userId, UUID.randomUUID());
    }

    public static String buildAssetImageKey(Long assetId) {
        return String.format("assets/%d/%s", assetId, UUID.randomUUID());
    }
}