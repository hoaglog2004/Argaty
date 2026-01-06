package com.argaty.controller.api;

import com.argaty. dto.response.ApiResponse;
import com. argaty.dto.response.FileUploadResponse;
import com. argaty.exception.BadRequestException;
import com.argaty.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * REST API Controller cho upload file
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadApiController {

    private final FileStorageService fileStorageService;

    /**
     * Upload single file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String directory,
            Principal principal) {

        // Yêu cầu đăng nhập
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập"));
        }

        try {
            String url = fileStorageService.uploadFile(file, directory + "/");
            return ResponseEntity.ok(ApiResponse. success("Upload thành công", 
                    FileUploadResponse.success(url)));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Upload multiple files
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "general") String directory,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập"));
        }

        if (files. size() > 10) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chỉ được upload tối đa 10 files"));
        }

        try {
            List<String> urls = fileStorageService.uploadFiles(files, directory + "/");
            return ResponseEntity.ok(ApiResponse.success("Upload thành công", 
                    FileUploadResponse.success(urls)));
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Upload ảnh sản phẩm (Admin)
     */
    @PostMapping("/upload/product")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        return uploadFile(file, "products", principal);
    }

    /**
     * Upload avatar
     */
    @PostMapping("/upload/avatar")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        return uploadFile(file, "avatars", principal);
    }

    /**
     * Upload ảnh review
     */
    @PostMapping("/upload/review")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadReviewImage(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        return uploadFile(file, "reviews", principal);
    }

    /**
     * Upload banner (Admin)
     */
    @PostMapping("/upload/banner")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadBanner(
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        return uploadFile(file, "banners", principal);
    }

    /**
     * Xóa file
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam String path,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Vui lòng đăng nhập"));
        }

        fileStorageService.deleteFile(path);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa file"));
    }
}