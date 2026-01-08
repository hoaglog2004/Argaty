package com.argaty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Custom Application Properties
 * Đọc các config từ application.properties với prefix "app"
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Upload upload = new Upload();
    private Pagination pagination = new Pagination();
    private Shipping shipping = new Shipping();
    private Review review = new Review();
    private Security security = new Security();

    @Data
    public static class Upload {
        private String dir = "uploads/";
        private String productImages = "uploads/products/";
        private String userAvatars = "uploads/avatars/";
        private String banners = "uploads/banners/";
        private String reviews = "uploads/reviews/";
    }

    @Data
    public static class Pagination {
        private int productsPerPage = 12;
        private int ordersPerPage = 10;
        private int reviewsPerPage = 5;
    }

    @Data
    public static class Shipping {
        private long defaultFee = 30000;
        private long freeThreshold = 500000;
    }

    @Data
    public static class Review {
        private boolean allowWithoutPurchase = false;
    }

    @Data
    public static class Security {
        private int passwordResetTokenExpiry = 30;
        private int emailVerifyTokenExpiry = 1440;
    }
}