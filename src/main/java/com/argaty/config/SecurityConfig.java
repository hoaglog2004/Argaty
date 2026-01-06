package com.argaty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Cấu hình Spring Security
 * Tạm thời cho phép tất cả - sẽ hoàn thiện ở bước sau
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password Encoder sử dụng BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình Security Filter Chain
     * TẠM THỜI: Cho phép tất cả để test
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF with cookie-based token repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Cấu hình authorize requests (cho phép khách xem toàn bộ site)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/home",
                    "/products/**",
                    "/static/**",
                    "/uploads/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/auth/**",
                    "/api/public/**"
                ).permitAll()

                // Admin routes - yêu cầu role ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Staff routes - yêu cầu role STAFF hoặc ADMIN
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")

                // Các request khác: cho phép tất cả (khách không cần đăng nhập)
                .anyRequest().permitAll()
            )
            
            // Cấu hình form login
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            
            // Cấu hình logout
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("ARGATY_SESSION")
                .permitAll()
            )
            
            // Cấu hình remember me
            .rememberMe(remember -> remember
                .key("argaty-remember-me-key")
                .tokenValiditySeconds(86400 * 7) // 7 ngày
                .rememberMeParameter("remember-me")
            )
            
            // Xử lý access denied
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}