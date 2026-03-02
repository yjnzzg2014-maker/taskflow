package com.taskflow.dto;

import com.taskflow.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class EventDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private String location;

        private Boolean isAllDay = false;

        private Event.RepeatType repeatType = Event.RepeatType.NONE;

        private String repeatRule;

        private LocalDateTime remindAt;

        private Set<Long> tagIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        private Boolean isAllDay;
        private Event.RepeatType repeatType;
        private String repeatRule;
        private LocalDateTime remindAt;
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
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        private Boolean isAllDay;
        private Event.RepeatType repeatType;
        private String repeatRule;
        private LocalDateTime remindAt;
        private Set<TagDTO.Response> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Event event) {
            return Response.builder()
                    .id(event.getId())
                    .userId(event.getUser().getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startTime(event.getStartTime())
                    .endTime(event.getEndTime())
                    .location(event.getLocation())
                    .isAllDay(event.getIsAllDay())
                    .repeatType(event.getRepeatType())
                    .repeatRule(event.getRepeatRule())
                    .remindAt(event.getRemindAt())
                    .tags(event.getTags().stream()
                            .map(TagDTO.Response::fromEntity)
                            .collect(Collectors.toSet()))
                    .createdAt(event.getCreatedAt())
                    .updatedAt(event.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarRequest {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
