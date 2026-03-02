package com.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class StatsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private Long totalTasks;
        private Long completedTasks;
        private Long pendingTasks;
        private Long inProgressTasks;
        private Long overdueTasks;
        private Long totalEvents;
        private Double completionRate;
        private Long todayEvents;
        private Long todayTasks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskTrend {
        private List<TrendData> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendData {
        private String date;
        private Long created;
        private Long completed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryDistribution {
        private List<CategoryData> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryData {
        private String name;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriorityDistribution {
        private List<PriorityData> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriorityData {
        private String priority;
        private Long count;
    }
}
