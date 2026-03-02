package com.taskflow.controller;

import com.taskflow.dto.PomodoroRecordDTO;
import com.taskflow.entity.PomodoroRecord;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.PomodoroRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pomodoros")
public class PomodoroController {

    @Autowired
    private PomodoroRecordService pomodoroRecordService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/records")
    public ResponseEntity<PomodoroRecordDTO> createRecord(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long taskId = request.get("taskId") != null ? Long.valueOf(request.get("taskId").toString()) : null;
        String modeStr = request.get("mode").toString();
        PomodoroRecord.PomodoroMode mode = PomodoroRecord.PomodoroMode.valueOf(modeStr);
        Integer duration = Integer.valueOf(request.get("duration").toString());
        String notes = (String) request.get("notes");

        PomodoroRecordDTO record = pomodoroRecordService.createRecord(user.getId(), taskId, mode, duration, notes);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/records")
    public ResponseEntity<List<PomodoroRecordDTO>> getRecords(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PomodoroRecordDTO> records = pomodoroRecordService.getUserRecords(user.getId());
        return ResponseEntity.ok(records);
    }

    @GetMapping("/stats")
    public ResponseEntity<PomodoroRecordService.PomodoroStatsDTO> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PomodoroRecordService.PomodoroStatsDTO stats = pomodoroRecordService.getStats(user.getId());
        return ResponseEntity.ok(stats);
    }
}
