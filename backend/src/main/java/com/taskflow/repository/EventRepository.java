package com.taskflow.repository;

import com.taskflow.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByUserIdOrderByStartTimeDesc(Long userId);

    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND e.startTime >= :start AND e.endTime <= :end ORDER BY e.startTime")
    List<Event> findByUserIdAndDateRange(@Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND e.startTime >= :start AND e.startTime < :end")
    List<Event> findByUserIdAndStartTimeBetween(@Param("userId") Long userId,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    List<Event> findByUserIdAndRemindAtBeforeAndRemindAtIsNotNull(Long userId, LocalDateTime before);
}
