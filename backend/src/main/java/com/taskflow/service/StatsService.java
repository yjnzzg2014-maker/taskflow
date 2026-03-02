package com.taskflow.service;

import com.taskflow.dto.StatsDTO;
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

        Long totalTasks = taskRepository.count();
        Long completedTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.COMPLETED);
        Long pendingTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.PENDING);
        Long inProgressTasks = taskRepository.countByUserIdAndStatus(userId, Task.TaskStatus.IN_PROGRESS);
        Long overdueTasks = (long) taskRepository.findOverdueTasks(userId, LocalDateTime.now()).size();

        Long totalEvents = eventRepository.count();
        List events = eventRepository.findByUserIdOrderByStartTimeDesc(userId);
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
}
