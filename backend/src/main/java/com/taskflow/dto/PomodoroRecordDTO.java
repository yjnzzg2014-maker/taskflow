package com.taskflow.dto;

import com.taskflow.entity.PomodoroRecord;
import java.time.LocalDateTime;

public class PomodoroRecordDTO {

    private Long id;
    private Long taskId;
    private String taskTitle;
    private PomodoroRecord.PomodoroMode mode;
    private Integer duration;
    private LocalDateTime completedAt;
    private String notes;

    public PomodoroRecordDTO() {}

    public PomodoroRecordDTO(Long id, Long taskId, String taskTitle,
                             PomodoroRecord.PomodoroMode mode, Integer duration,
                             LocalDateTime completedAt, String notes) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.mode = mode;
        this.duration = duration;
        this.completedAt = completedAt;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public PomodoroRecord.PomodoroMode getMode() {
        return mode;
    }

    public void setMode(PomodoroRecord.PomodoroMode mode) {
        this.mode = mode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
