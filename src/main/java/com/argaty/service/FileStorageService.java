package com.argaty.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface cho File Storage
 */
public interface FileStorageService {

    String uploadFile(MultipartFile file, String directory);

    List<String> uploadFiles(List<MultipartFile> files, String directory);

    void deleteFile(String filePath);

    void deleteFiles(List<String> filePaths);

    boolean isValidImageFile(MultipartFile file);

    String getFileExtension(String filename);
}