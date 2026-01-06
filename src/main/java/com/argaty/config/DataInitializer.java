package com.argaty.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Khởi tạo dữ liệu và cấu trúc thư mục khi ứng dụng start
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppProperties appProperties;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Initializing Argaty application...");
        
        // Tạo các thư mục upload
        createUploadDirectories();
        
        log.info("✅ Initialization completed!");
    }

    /**
     * Tạo các thư mục upload nếu chưa tồn tại
     */
    private void createUploadDirectories() {
        String[] directories = {
            appProperties.getUpload().getDir(),
            appProperties.getUpload().getProductImages(),
            appProperties.getUpload().getUserAvatars(),
            appProperties.getUpload().getBanners(),
            appProperties.getUpload().getReviews()
        };

        for (String dir : directories) {
            try {
                Path path = Paths.get(dir);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    log.info("📁 Created directory: {}", dir);
                }
            } catch (IOException e) {
                log.error("❌ Failed to create directory: {}", dir, e);
            }
        }
    }
}