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
     * 
     * PUBLIC: Home, Products, Categories, About, Contact, FAQ, Auth, Static
     * PROTECTED: Cart, Checkout, Profile, Orders, Wishlist
     * ADMIN: Dashboard and management
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF with cookie-based token repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            
            // Cấu hình authorize requests
            .authorizeHttpRequests(auth -> auth
                // PUBLIC - No authentication required
                .requestMatchers(
                    "/",
                    "/home",
                    "/about",
                    "/contact",
                    "/faq",
                    "/policy/**",
                    "/products",
                    "/products/**",
                    "/categories/**",
                    "/brands/**",
                    "/auth/**",
                    "/api/public/**",
                    "/static/**",
                    "/uploads/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // ADMIN ONLY - Requires ADMIN role
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // PROTECTED - Requires authentication (any authenticated user)
                .requestMatchers(
                    "/cart/**",
                    "/checkout/**",
                    "/profile/**",
                    "/wishlist/**",
                    "/api/cart/**",
                    "/api/wishlist/**",
                    "/api/address/**",
                    "/api/reviews/**",
                    "/api/notifications/**"
                ).authenticated()

                // All other requests - deny by default (secure by default)
                .anyRequest().authenticated()
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