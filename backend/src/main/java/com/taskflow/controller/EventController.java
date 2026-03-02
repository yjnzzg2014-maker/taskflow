package com.taskflow.controller;

import com.taskflow.dto.EventDTO;
import com.taskflow.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDTO.Response>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO.Response> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<EventDTO.Response>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        EventDTO.CalendarRequest request = EventDTO.CalendarRequest.builder()
                .start(start)
                .end(end)
                .build();
        return ResponseEntity.ok(eventService.getCalendarEvents(request));
    }

    @PostMapping
    public ResponseEntity<EventDTO.Response> createEvent(@Valid @RequestBody EventDTO.CreateRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTO.Response> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO.UpdateRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
