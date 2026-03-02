package com.taskflow.service;

import com.taskflow.dto.TagDTO;
import com.taskflow.entity.Tag;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.TagRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<TagDTO.Response> getAllTags() {
        User user = getCurrentUser();
        return tagRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(TagDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public TagDTO.Response getTagById(Long id) {
        User user = getCurrentUser();
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this tag");
        }

        return TagDTO.Response.fromEntity(tag);
    }

    @Transactional
    public TagDTO.Response createTag(TagDTO.CreateRequest request) {
        User user = getCurrentUser();

        if (tagRepository.existsByUserIdAndName(user.getId(), request.getName())) {
            throw new BadRequestException("Tag with this name already exists");
        }

        Tag tag = Tag.builder()
                .user(user)
                .name(request.getName())
                .color(request.getColor())
                .build();

        tag = tagRepository.save(tag);
        return TagDTO.Response.fromEntity(tag);
    }

    @Transactional
    public TagDTO.Response updateTag(Long id, TagDTO.UpdateRequest request) {
        User user = getCurrentUser();
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this tag");
        }

        if (request.getName() != null) {
            if (tagRepository.existsByUserIdAndName(user.getId(), request.getName())) {
                throw new BadRequestException("Tag with this name already exists");
            }
            tag.setName(request.getName());
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        tag = tagRepository.save(tag);
        return TagDTO.Response.fromEntity(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        User user = getCurrentUser();
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this tag");
        }

        tagRepository.delete(tag);
    }
}
