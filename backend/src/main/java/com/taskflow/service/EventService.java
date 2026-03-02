package com.taskflow.service;

import com.taskflow.dto.EventDTO;
import com.taskflow.entity.Event;
import com.taskflow.entity.Tag;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.EventRepository;
import com.taskflow.repository.TagRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<EventDTO.Response> getAllEvents() {
        User user = getCurrentUser();
        return eventRepository.findByUserIdOrderByStartTimeDesc(user.getId())
                .stream()
                .map(EventDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public EventDTO.Response getEventById(Long id) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (!event.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this event");
        }

        return EventDTO.Response.fromEntity(event);
    }

    public List<EventDTO.Response> getEventsByDateRange(LocalDateTime start, LocalDateTime end) {
        User user = getCurrentUser();
        return eventRepository.findByUserIdAndDateRange(user.getId(), start, end)
                .stream()
                .map(EventDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EventDTO.Response> getCalendarEvents(EventDTO.CalendarRequest request) {
        User user = getCurrentUser();
        return eventRepository.findByUserIdAndStartTimeBetween(user.getId(), request.getStart(), request.getEnd())
                .stream()
                .map(EventDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO.Response createEvent(EventDTO.CreateRequest request) {
        User user = getCurrentUser();

        Event event = Event.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .isAllDay(request.getIsAllDay() != null ? request.getIsAllDay() : false)
                .repeatType(request.getRepeatType() != null ? request.getRepeatType() : Event.RepeatType.NONE)
                .repeatRule(request.getRepeatRule())
                .remindAt(request.getRemindAt())
                .build();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            event.setTags(tags);
        }

        event = eventRepository.save(event);
        return EventDTO.Response.fromEntity(event);
    }

    @Transactional
    public EventDTO.Response updateEvent(Long id, EventDTO.UpdateRequest request) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (!event.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this event");
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getIsAllDay() != null) {
            event.setIsAllDay(request.getIsAllDay());
        }
        if (request.getRepeatType() != null) {
            event.setRepeatType(request.getRepeatType());
        }
        if (request.getRepeatRule() != null) {
            event.setRepeatRule(request.getRepeatRule());
        }
        if (request.getRemindAt() != null) {
            event.setRemindAt(request.getRemindAt());
        }
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            event.setTags(tags);
        }

        event = eventRepository.save(event);
        return EventDTO.Response.fromEntity(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        if (!event.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this event");
        }

        eventRepository.delete(event);
    }
}
