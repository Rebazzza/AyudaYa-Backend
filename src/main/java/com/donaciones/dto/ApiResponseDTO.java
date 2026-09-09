package com.donaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
    private String error;
    private T data;

    public static <T> ApiResponseDTO<T> success(HttpStatus status, String message, String path, T data) {
        return ApiResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .path(path)
                .data(data)
                .build();
    }

    public static ApiResponseDTO<Void> error(HttpStatus status, String message, String path, String error) {
        return ApiResponseDTO.<Void>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .path(path)
                .error(error)
                .build();
    }

}