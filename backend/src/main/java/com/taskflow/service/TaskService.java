package com.taskflow.service;

import com.taskflow.dto.TaskDTO;
import com.taskflow.dto.SubTaskDTO;
import com.taskflow.entity.*;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<TaskDTO.Response> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(TaskDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<TaskDTO.Response> getTasks(Pageable pageable) {
        User user = getCurrentUser();
        return taskRepository.findByUserId(user.getId(), pageable)
                .map(TaskDTO.Response::fromEntity);
    }

    public TaskDTO.Response getTaskById(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this task");
        }

        return TaskDTO.Response.fromEntity(task);
    }

    public List<TaskDTO.Response> getTasksByStatus(Task.TaskStatus status) {
        User user = getCurrentUser();
        return taskRepository.findByUserIdAndStatus(user.getId(), status)
                .stream()
                .map(TaskDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TaskDTO.Response> getTasksByFilter(TaskDTO.FilterRequest filter) {
        User user = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return tasks.stream()
                .filter(task -> filter.getPriority() == null || task.getPriority() == filter.getPriority())
                .filter(task -> filter.getStatus() == null || task.getStatus() == filter.getStatus())
                .filter(task -> filter.getCategoryId() == null ||
                        (task.getCategory() != null && task.getCategory().getId().equals(filter.getCategoryId())))
                .filter(task -> filter.getDueDateFrom() == null ||
                        (task.getDueDate() != null && task.getDueDate().isAfter(filter.getDueDateFrom())))
                .filter(task -> filter.getDueDateTo() == null ||
                        (task.getDueDate() != null && task.getDueDate().isBefore(filter.getDueDateTo())))
                .filter(task -> filter.getKeyword() == null ||
                        task.getTitle().toLowerCase().contains(filter.getKeyword().toLowerCase()) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(filter.getKeyword().toLowerCase())))
                .map(TaskDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO.Response createTask(TaskDTO.CreateRequest request) {
        User user = getCurrentUser();

        Task task = Task.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
                .status(request.getStatus() != null ? request.getStatus() : Task.TaskStatus.PENDING)
                .dueDate(request.getDueDate())
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Category does not belong to user");
            }
            task.setCategory(category);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            task.setTags(tags);
        }

        task = taskRepository.save(task);
        return TaskDTO.Response.fromEntity(task);
    }

    @Transactional
    public TaskDTO.Response updateTask(Long id, TaskDTO.UpdateRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this task");
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!category.getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Category does not belong to user");
            }
            task.setCategory(category);
        }
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            task.setTags(tags);
        }

        task = taskRepository.save(task);
        return TaskDTO.Response.fromEntity(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this task");
        }

        taskRepository.delete(task);
    }

    // SubTask methods
    @Transactional
    public SubTaskDTO.Response addSubTask(Long taskId, SubTaskDTO.CreateRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to add sub-task to this task");
        }

        SubTask subTask = SubTask.builder()
                .task(task)
                .title(request.getTitle())
                .isCompleted(false)
                .build();

        subTask = subTaskRepository.save(subTask);
        return SubTaskDTO.Response.fromEntity(subTask);
    }

    @Transactional
    public SubTaskDTO.Response updateSubTask(Long taskId, Long subTaskId, SubTaskDTO.UpdateRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this sub-task");
        }

        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("SubTask not found with id: " + subTaskId));

        if (request.getTitle() != null) {
            subTask.setTitle(request.getTitle());
        }
        if (request.getIsCompleted() != null) {
            subTask.setIsCompleted(request.getIsCompleted());
        }

        subTask = subTaskRepository.save(subTask);
        return SubTaskDTO.Response.fromEntity(subTask);
    }

    @Transactional
    public void deleteSubTask(Long taskId, Long subTaskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this sub-task");
        }

        subTaskRepository.deleteById(subTaskId);
    }

    public List<TaskDTO.Response> searchTasks(String keyword) {
        User user = getCurrentUser();
        return taskRepository.searchByKeyword(user.getId(), keyword)
                .stream()
                .map(TaskDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }
}
