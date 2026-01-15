package com.argaty.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình Web MVC
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Cấu hình static resources và upload folder
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS files
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        // JS files
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        // Images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // Static resources (all)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Upload folder - cho phép truy cập file đã upload
        // Sử dụng đường dẫn tuyệt đối với prefix file:///
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        String uploadAbsolutePath = "file:///" + uploadPath.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadAbsolutePath);
    }

    /**
     * Cấu hình các view controller đơn giản
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect root to home
        registry.addViewController("/").setViewName("redirect:/home");
    }
}