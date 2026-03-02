package com.taskflow.service;

import com.taskflow.dto.PomodoroRecordDTO;
import com.taskflow.entity.PomodoroRecord;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.repository.PomodoroRecordRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PomodoroRecordService {

    @Autowired
    private PomodoroRecordRepository pomodoroRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public PomodoroRecordDTO createRecord(Long userId, Long taskId, PomodoroRecord.PomodoroMode mode, Integer duration, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElse(null);
        }

        PomodoroRecord record = new PomodoroRecord();
        record.setUser(user);
        record.setTask(task);
        record.setMode(mode);
        record.setDuration(duration);
        record.setCompletedAt(LocalDateTime.now());
        record.setNotes(notes);

        record = pomodoroRecordRepository.save(record);

        return toDTO(record);
    }

    public List<PomodoroRecordDTO> getUserRecords(Long userId) {
        return pomodoroRecordRepository.findByUserIdOrderByCompletedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PomodoroRecordDTO> getTaskRecords(Long userId, Long taskId) {
        return pomodoroRecordRepository.findByUserIdAndTaskId(userId, taskId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PomodoroStatsDTO getStats(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Long todaySessions = pomodoroRecordRepository.countTodayWorkSessions(userId, startOfDay);
        Long weekSessions = pomodoroRecordRepository.countWorkSessions(userId, startOfWeek);
        Long monthSessions = pomodoroRecordRepository.countWorkSessions(userId, startOfMonth);
        Long todayMinutes = pomodoroRecordRepository.sumWorkDuration(userId, startOfDay);
        Long weekMinutes = pomodoroRecordRepository.sumWorkDuration(userId, startOfWeek);
        Long monthMinutes = pomodoroRecordRepository.sumWorkDuration(userId, startOfMonth);

        return new PomodoroStatsDTO(
                todaySessions != null ? todaySessions.intValue() : 0,
                weekSessions != null ? weekSessions.intValue() : 0,
                monthSessions != null ? monthSessions.intValue() : 0,
                todayMinutes != null ? todayMinutes.intValue() : 0,
                weekMinutes != null ? weekMinutes.intValue() : 0,
                monthMinutes != null ? monthMinutes.intValue() : 0
        );
    }

    private PomodoroRecordDTO toDTO(PomodoroRecord record) {
        String taskTitle = record.getTask() != null ? record.getTask().getTitle() : null;
        Long taskId = record.getTask() != null ? record.getTask().getId() : null;
        return new PomodoroRecordDTO(
                record.getId(),
                taskId,
                taskTitle,
                record.getMode(),
                record.getDuration(),
                record.getCompletedAt(),
                record.getNotes()
        );
    }

    public static class PomodoroStatsDTO {
        private int todaySessions;
        private int weekSessions;
        private int monthSessions;
        private int todayMinutes;
        private int weekMinutes;
        private int monthMinutes;

        public PomodoroStatsDTO(int todaySessions, int weekSessions, int monthSessions,
                                 int todayMinutes, int weekMinutes, int monthMinutes) {
            this.todaySessions = todaySessions;
            this.weekSessions = weekSessions;
            this.monthSessions = monthSessions;
            this.todayMinutes = todayMinutes;
            this.weekMinutes = weekMinutes;
            this.monthMinutes = monthMinutes;
        }

        public int getTodaySessions() { return todaySessions; }
        public int getWeekSessions() { return weekSessions; }
        public int getMonthSessions() { return monthSessions; }
        public int getTodayMinutes() { return todayMinutes; }
        public int getWeekMinutes() { return weekMinutes; }
        public int getMonthMinutes() { return monthMinutes; }
    }
}
