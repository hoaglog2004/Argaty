package com. argaty.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho response upload file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadResponse {

    private Boolean success;
    private String message;
    private String url;
    private List<String> urls;
    private String fileName;
    private Long fileSize;
    private String fileType;

    public static FileUploadResponse success(String url) {
        return FileUploadResponse.builder()
                .success(true)
                .url(url)
                .build();
    }

    public static FileUploadResponse success(List<String> urls) {
        return FileUploadResponse.builder()
                .success(true)
                .urls(urls)
                .build();
    }

    public static FileUploadResponse error(String message) {
        return FileUploadResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}