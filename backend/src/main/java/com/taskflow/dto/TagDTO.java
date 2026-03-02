package com.taskflow.dto;

import com.taskflow.entity.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class TagDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String name;
        private String color;
        private LocalDateTime createdAt;

        public static Response fromEntity(Tag tag) {
            return Response.builder()
                    .id(tag.getId())
                    .userId(tag.getUser().getId())
                    .name(tag.getName())
                    .color(tag.getColor())
                    .createdAt(tag.getCreatedAt())
                    .build();
        }
    }
}
