package com.rms.admin.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void uploadFile(MultipartFile file, String objectKey);
    void deleteFile(String objectKey);
    String generatePresignedUrl(String objectKey, int expiryMinutes);
}