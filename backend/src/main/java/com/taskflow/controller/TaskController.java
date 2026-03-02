package com.taskflow.controller;

import com.taskflow.dto.TaskDTO;
import com.taskflow.dto.SubTaskDTO;
import com.taskflow.entity.Task;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDTO.Response>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<TaskDTO.Response>> getTasks(Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO.Response> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDTO.Response>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<TaskDTO.Response>> getTasksByFilter(@RequestBody TaskDTO.FilterRequest filter) {
        return ResponseEntity.ok(taskService.getTasksByFilter(filter));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskDTO.Response>> searchTasks(@RequestParam String keyword) {
        return ResponseEntity.ok(taskService.searchTasks(keyword));
    }

    @PostMapping
    public ResponseEntity<TaskDTO.Response> createTask(@Valid @RequestBody TaskDTO.CreateRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO.Response> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO.UpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // SubTask endpoints
    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<SubTaskDTO.Response> addSubTask(
            @PathVariable Long taskId,
            @RequestBody SubTaskDTO.CreateRequest request) {
        return ResponseEntity.ok(taskService.addSubTask(taskId, request));
    }

    @PutMapping("/{taskId}/subtasks/{subTaskId}")
    public ResponseEntity<SubTaskDTO.Response> updateSubTask(
            @PathVariable Long taskId,
            @PathVariable Long subTaskId,
            @RequestBody SubTaskDTO.UpdateRequest request) {
        return ResponseEntity.ok(taskService.updateSubTask(taskId, subTaskId, request));
    }

    @DeleteMapping("/{taskId}/subtasks/{subTaskId}")
    public ResponseEntity<Void> deleteSubTask(
            @PathVariable Long taskId,
            @PathVariable Long subTaskId) {
        taskService.deleteSubTask(taskId, subTaskId);
        return ResponseEntity.noContent().build();
    }
}
