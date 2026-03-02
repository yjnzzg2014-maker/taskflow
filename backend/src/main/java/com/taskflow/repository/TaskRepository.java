package com.taskflow.repository;

import com.taskflow.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByUserId(Long userId, Pageable pageable);

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, Task.TaskStatus status);

    List<Task> findByUserIdAndPriority(Long userId, Task.Priority priority);

    List<Task> findByUserIdAndDueDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Task.TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.dueDate <= :date")
    Long countByUserIdAndDueDateBefore(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.dueDate IS NOT NULL AND t.dueDate <= :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
