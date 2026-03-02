package com.taskflow.dto;

import com.taskflow.entity.SubTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class SubTaskDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private Boolean isCompleted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long taskId;
        private String title;
        private Boolean isCompleted;
        private LocalDateTime createdAt;

        public static Response fromEntity(SubTask subTask) {
            return Response.builder()
                    .id(subTask.getId())
                    .taskId(subTask.getTask().getId())
                    .title(subTask.getTitle())
                    .isCompleted(subTask.getIsCompleted())
                    .createdAt(subTask.getCreatedAt())
                    .build();
        }
    }
}
