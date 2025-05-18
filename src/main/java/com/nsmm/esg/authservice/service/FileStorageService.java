package com.nsmm.esg.authservice.service;

import com.nsmm.esg.authservice.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final String FOLDER = "profile-images/";

    /**
     * S3에 프로필 이미지 업로드
     *
     * @param file Multipart 업로드 파일
     * @return 전체 URL (https://bucket.s3.region.amazonaws.com/key)
     */
    public String store(MultipartFile file) {
        log.debug("S3 이미지 업로드 시도: 원본명={}, 크기={}KB", file.getOriginalFilename(), file.getSize() / 1024);

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new FileStorageException("파일명이 비어 있습니다.");
            }

            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!isValidImageExtension(ext)) {
                throw new FileStorageException("지원하지 않는 이미지 형식입니다. 지원 형식: .jpg, .jpeg, .png, .gif");
            }

            String key = FOLDER + UUID.randomUUID() + ext;

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
            log.info("✅ S3 파일 업로드 성공: key={}, url={}", key, url);

            return url;

        } catch (IOException e) {
            log.error("❌ S3 업로드 실패: {}", e.getMessage(), e);
            throw new FileStorageException("이미지 업로드 실패: " + e.getMessage(), e);
        }
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals(".jpg") ||
                extension.equals(".jpeg") ||
                extension.equals(".png") ||
                extension.equals(".gif");
    }
}