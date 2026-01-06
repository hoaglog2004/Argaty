package com.argaty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Argaty - Gaming Gear E-commerce Application
 * 
 * @author Argaty Team
 * @version 1.0.0
 */
@SpringBootApplication
public class ArgatyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArgatyApplication.class, args);
        System.out.println("🚀 ========================================");
        System.out.println("🎮 ARGATY - Gaming Gear Store");
        System.out.println("🌐 http://localhost:8080");
        System.out.println("🚀 ========================================");
    }
}