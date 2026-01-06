package com.argaty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

/**
 * Cấu hình Thymeleaf
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Thêm Layout Dialect để sử dụng layout chung
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}