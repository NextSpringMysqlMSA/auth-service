/**
 * 파일 저장 서비스
 * - 프로필 이미지 등 파일 업로드 처리 및 저장 담당
 */
package com.nsmm.esg.authservice.service;

import com.nsmm.esg.authservice.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/images/";

    /**
     * 파일 저장 메서드
     * @param file 업로드된 파일
     * @return 저장된 파일의 URL 경로
     */
    public String store(MultipartFile file) {
        log.debug("파일 저장 시도: 파일명={}, 크기={}KB", 
                file.getOriginalFilename(), file.getSize() / 1024);
        
        try {
            // 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug("업로드 디렉토리 생성: {}", uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new FileStorageException("파일명이 비어 있습니다.");
            }
            
            // 파일 확장자 확인
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 허용된 확장자 체크 (이미지만 허용)
            if (!isValidImageExtension(ext)) {
                throw new FileStorageException("지원하지 않는 이미지 형식입니다. 지원 형식: .jpg, .jpeg, .png, .gif");
            }
            
            // 고유 파일명 생성
            String savedFilename = UUID.randomUUID() + ext;
            
            // 파일 저장
            File destination = new File(uploadDir + savedFilename);
            file.transferTo(destination);
            
            log.info("파일 저장 성공: 원본명={}, 저장명={}, 경로={}", 
                    originalFilename, savedFilename, destination.getAbsolutePath());
            
            // 프론트에서 접근할 수 있도록 /images 경로 반환
            return "/images/" + savedFilename;
        } catch (IOException e) {
            log.error("파일 저장 실패: 파일명={}, 원인={}", file.getOriginalFilename(), e.getMessage(), e);
            throw new FileStorageException("파일 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }
    
    /**
     * 유효한 이미지 확장자인지 확인
     */
    private boolean isValidImageExtension(String extension) {
        extension = extension.toLowerCase();
        return extension.equals(".jpg") || 
               extension.equals(".jpeg") || 
               extension.equals(".png") || 
               extension.equals(".gif");
    }
}
