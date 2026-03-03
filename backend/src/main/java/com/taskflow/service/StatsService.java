package com.taskflow.service;

import com.taskflow.dto.StatsDTO;
import com.taskflow.entity.Event;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public StatsDTO.DashboardStats getDashboardStats() {
        User user = getCurrentUser();
        Long userId = user.getId();

        Long completedTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.COMPLETED);
        Long pendingTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.PENDING);
        Long inProgressTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.IN_PROGRESS);
        Long totalTasks = completedTasks + pendingTasks + inProgressTasks;
        Long overdueTasks = (long) taskRepository.findOverdueTasks(userId, LocalDateTime.now()).size();

        List events = eventRepository.findByUserIdOrderByStartTimeDesc(userId);
        Long totalEvents = (long) events.size();
        Long todayEvents = (long) events.stream()
                .filter(e -> {
                    var event = (com.taskflow.entity.Event) e;
                    return event.getStartTime().toLocalDate().equals(LocalDate.now());
                })
                .count();

        List<Task> todayTasksList = taskRepository.findByUserIdAndDueDateBetween(
                userId,
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN),
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
        );
        Long todayTasks = (long) todayTasksList.size();

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        return StatsDTO.DashboardStats.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .overdueTasks(overdueTasks)
                .totalEvents(totalEvents)
                .todayEvents(todayEvents)
                .todayTasks(todayTasks)
                .completionRate(completionRate)
                .build();
    }

    public StatsDTO.TaskTrend getTaskTrend(int days) {
        User user = getCurrentUser();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<StatsDTO.TrendData> trendData = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = currentDate.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);

            List<Task> createdTasks = taskRepository.findByUserIdAndDueDateBetween(user.getId(), dayStart, dayEnd);
            Long completedCount = createdTasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                    .count();

            trendData.add(StatsDTO.TrendData.builder()
                    .date(date.toString())
                    .created((long) createdTasks.size())
                    .completed(completedCount)
                    .build());
        }

        return StatsDTO.TaskTrend.builder()
                .data(trendData)
                .build();
    }

    public StatsDTO.CategoryDistribution getCategoryDistribution() {
        User user = getCurrentUser();

        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<String, Long> categoryCount = tasks.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(), Collectors.counting()));

        List<StatsDTO.CategoryData> data = categoryCount.entrySet().stream()
                .map(entry -> StatsDTO.CategoryData.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return StatsDTO.CategoryDistribution.builder()
                .data(data)
                .build();
    }

    public StatsDTO.PriorityDistribution getPriorityDistribution() {
        User user = getCurrentUser();

        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Map<String, Long> priorityCount = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        List<StatsDTO.PriorityData> data = priorityCount.entrySet().stream()
                .map(entry -> StatsDTO.PriorityData.builder()
                        .priority(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return StatsDTO.PriorityDistribution.builder()
                .data(data)
                .build();
    }

    public Map<String, Object> getProductivityReport(int days) {
        User user = getCurrentUser();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Task> recentTasks = tasks.stream()
                .filter(t -> t.getCreatedAt().isAfter(startDate))
                .collect(Collectors.toList());

        // Calculate productivity metrics
        long totalTasks = recentTasks.size();
        long completedTasks = recentTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                .count();
        long overdueTasks = recentTasks.stream()
                .filter(t -> t.getStatus() != Task.TaskStatus.COMPLETED)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()))
                .count();

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        // Average completion time (for completed tasks)
        List<Task> completedWithTime = recentTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalTasks", totalTasks);
        response.put("completedTasks", completedTasks);
        response.put("overdueTasks", overdueTasks);
        response.put("completionRate", completionRate);
        response.put("period", days);

        // Daily average
        response.put("dailyAverage", totalTasks > 0 ? (double) totalTasks / days : 0);

        // Streak calculation
        int streak = calculateStreak(user.getId());
        response.put("currentStreak", streak);

        return response;
    }

    public Map<String, Object> getMonthlyStats(int year, int month) {
        User user = getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<Task> monthlyTasks = tasks.stream()
                .filter(t -> t.getCreatedAt().isAfter(startDateTime) && t.getCreatedAt().isBefore(endDateTime))
                .collect(Collectors.toList());

        long totalTasks = monthlyTasks.size();
        long completedTasks = monthlyTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED)
                .count();

        List<Event> events = eventRepository.findByUserIdOrderByStartTimeDesc(user.getId());
        long totalEvents = events.stream()
                .filter(e -> e.getStartTime().isAfter(startDateTime) && e.getStartTime().isBefore(endDateTime))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("month", month);
        response.put("totalTasks", totalTasks);
        response.put("completedTasks", completedTasks);
        response.put("totalEvents", totalEvents);
        response.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);

        return response;
    }

    private int calculateStreak(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);

            List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, dayStart, dayEnd);
            boolean hasCompletedTask = tasks.stream()
                    .anyMatch(t -> t.getStatus() == Task.TaskStatus.COMPLETED);

            if (hasCompletedTask) {
                streak++;
            } else if (i > 0) {
                break;
            }
        }

        return streak;
    }
}
