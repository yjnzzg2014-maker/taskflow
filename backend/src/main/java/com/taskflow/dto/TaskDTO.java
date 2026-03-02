package com.taskflow.dto;

import com.taskflow.entity.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        private Task.Priority priority = Task.Priority.MEDIUM;

        private Task.TaskStatus status = Task.TaskStatus.PENDING;

        private LocalDateTime dueDate;

        private Long categoryId;

        private Set<Long> tagIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.TaskStatus status;
        private LocalDateTime dueDate;
        private Long categoryId;
        private Set<Long> tagIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String title;
        private String description;
        private Task.Priority priority;
        private Task.TaskStatus status;
        private LocalDateTime dueDate;
        private CategoryDTO.Response category;
        private Set<SubTaskDTO.Response> subTasks;
        private Set<TagDTO.Response> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Task task) {
            return Response.builder()
                    .id(task.getId())
                    .userId(task.getUser().getId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .priority(task.getPriority())
                    .status(task.getStatus())
                    .dueDate(task.getDueDate())
                    .category(task.getCategory() != null ? CategoryDTO.Response.fromEntity(task.getCategory()) : null)
                    .subTasks(task.getSubTasks().stream()
                            .map(SubTaskDTO.Response::fromEntity)
                            .collect(Collectors.toSet()))
                    .tags(task.getTags().stream()
                            .map(TagDTO.Response::fromEntity)
                            .collect(Collectors.toSet()))
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterRequest {
        private Task.Priority priority;
        private Task.TaskStatus status;
        private Long categoryId;
        private Long tagId;
        private LocalDateTime dueDateFrom;
        private LocalDateTime dueDateTo;
        private String keyword;
    }
}
