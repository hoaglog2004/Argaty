package com.argaty. dto.response;

import com. fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response đăng nhập/đăng ký
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class AuthResponse {

    private Boolean success;
    private String message;
    private UserResponse user;
    private String redirectUrl;

    public static AuthResponse success(UserResponse user, String redirectUrl) {
        return AuthResponse.builder()
                .success(true)
                .user(user)
                .redirectUrl(redirectUrl)
                .build();
    }

    public static AuthResponse success(String message) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}