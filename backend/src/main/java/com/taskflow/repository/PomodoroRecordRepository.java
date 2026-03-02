package com.taskflow.repository;

import com.taskflow.entity.PomodoroRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PomodoroRecordRepository extends JpaRepository<PomodoroRecord, Long> {

    List<PomodoroRecord> findByUserIdOrderByCompletedAtDesc(Long userId);

    List<PomodoroRecord> findByUserIdAndTaskId(Long userId, Long taskId);

    @Query("SELECT COUNT(p) FROM PomodoroRecord p WHERE p.user.id = :userId AND p.mode = 'WORK' AND p.completedAt >= :startDate")
    Long countTodayWorkSessions(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(p) FROM PomodoroRecord p WHERE p.user.id = :userId AND p.mode = 'WORK' AND p.completedAt >= :startDate")
    Long countWorkSessions(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(p.duration) FROM PomodoroRecord p WHERE p.user.id = :userId AND p.mode = 'WORK' AND p.completedAt >= :startDate")
    Long sumWorkDuration(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT p.task.id, COUNT(p) FROM PomodoroRecord p WHERE p.user.id = :userId AND p.task IS NOT NULL AND p.mode = 'WORK' GROUP BY p.task.id")
    List<Object[]> countByTask(@Param("userId") Long userId);
}
