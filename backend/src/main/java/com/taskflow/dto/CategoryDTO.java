package com.taskflow.dto;

import com.taskflow.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CategoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String name;
        private LocalDateTime createdAt;

        public static Response fromEntity(Category category) {
            return Response.builder()
                    .id(category.getId())
                    .userId(category.getUser().getId())
                    .name(category.getName())
                    .createdAt(category.getCreatedAt())
                    .build();
        }
    }
}
