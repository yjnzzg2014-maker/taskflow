package com.taskflow.controller;

import com.taskflow.dto.TagDTO;
import com.taskflow.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO.Response>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO.Response> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping
    public ResponseEntity<TagDTO.Response> createTag(@Valid @RequestBody TagDTO.CreateRequest request) {
        return ResponseEntity.ok(tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDTO.Response> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagDTO.UpdateRequest request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
