package com.argaty.service.impl;

import com.argaty. config.AppProperties;
import com.argaty. exception.BadRequestException;
import com.argaty.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org. springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file. Paths;
import java.nio. file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implementation của FileStorageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final AppProperties appProperties;

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        // Validate file
        if (!isValidImageFile(file)) {
            throw new BadRequestException("Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Kích thước file không được vượt quá 10MB");
        }

        try {
            // Tạo tên file unique
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            // Tạo đường dẫn
            Path uploadPath = Paths.get(appProperties.getUpload().getDir() + directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/uploads/" + directory + newFilename;
            log.info("Uploaded file: {}", relativePath);

            return relativePath;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new BadRequestException("Không thể upload file: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files, String directory) {
        List<String> uploadedPaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String path = uploadFile(file, directory);
                uploadedPaths.add(path);
            }
        }

        return uploadedPaths;
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // Chuyển đổi từ URL path sang file path
            String actualPath = filePath.replace("/uploads/", appProperties.getUpload().getDir());
            Path path = Paths. get(actualPath);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted file: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", filePath, e. getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            deleteFile(filePath);
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }
}