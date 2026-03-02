package com.taskflow.controller;

import com.taskflow.dto.StatsDTO;
import com.taskflow.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        StatsDTO.DashboardStats stats = statsService.getDashboardStats();
        StatsDTO.TaskTrend trend = statsService.getTaskTrend(7);
        StatsDTO.PriorityDistribution priorityDist = statsService.getPriorityDistribution();
        StatsDTO.CategoryDistribution categoryDist = statsService.getCategoryDistribution();

        Map<String, Object> response = new HashMap<>();
        response.put("stats", stats);
        response.put("trend", trend);
        response.put("priorityDistribution", priorityDist);
        response.put("categoryDistribution", categoryDist);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trend")
    public ResponseEntity<StatsDTO.TaskTrend> getTaskTrend(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(statsService.getTaskTrend(days));
    }

    @GetMapping("/priority-distribution")
    public ResponseEntity<StatsDTO.PriorityDistribution> getPriorityDistribution() {
        return ResponseEntity.ok(statsService.getPriorityDistribution());
    }

    @GetMapping("/category-distribution")
    public ResponseEntity<StatsDTO.CategoryDistribution> getCategoryDistribution() {
        return ResponseEntity.ok(statsService.getCategoryDistribution());
    }
}
