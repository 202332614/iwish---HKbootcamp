package com.hackathon.wishlist.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 업로드 이미지 저장.
 * 프로젝트 루트의 'uploads' 폴더에 저장하고, 공개 경로(/uploads/파일명)를 반환한다.
 * (정적 서빙은 WebConfig 의 리소스 핸들러가 담당)
 */
@Service
public class FileStorageService {

    public static final String UPLOAD_DIR = "uploads";

    private final Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 폴더 생성 실패: " + root, e);
        }
    }

    /** 이미지 파일을 저장하고 공개 경로(/uploads/...)를 반환. 이미지가 아니면 400. */
    public String store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있어요.");
        }
        String ext = extensionOf(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 파일명입니다.");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했어요.", e);
        }
        return "/" + UPLOAD_DIR + "/" + filename;
    }

    /** 확장자: 원본 파일명 우선, 없으면 content-type 으로 추정. */
    private String extensionOf(String original, String contentType) {
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                String e = original.substring(dot).toLowerCase();
                if (e.matches("\\.[a-z0-9]{1,5}")) {
                    return e;
                }
            }
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
