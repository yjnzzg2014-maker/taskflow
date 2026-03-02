package com.taskflow.service;

import com.taskflow.dto.CategoryDTO;
import com.taskflow.entity.Category;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CategoryRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<CategoryDTO.Response> getAllCategories() {
        User user = getCurrentUser();
        return categoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(CategoryDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryDTO.Response getCategoryById(Long id) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to access this category");
        }

        return CategoryDTO.Response.fromEntity(category);
    }

    @Transactional
    public CategoryDTO.Response createCategory(CategoryDTO.CreateRequest request) {
        User user = getCurrentUser();

        if (categoryRepository.existsByUserIdAndName(user.getId(), request.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .build();

        category = categoryRepository.save(category);
        return CategoryDTO.Response.fromEntity(category);
    }

    @Transactional
    public CategoryDTO.Response updateCategory(Long id, CategoryDTO.UpdateRequest request) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this category");
        }

        if (request.getName() != null) {
            if (categoryRepository.existsByUserIdAndName(user.getId(), request.getName())) {
                throw new BadRequestException("Category with this name already exists");
            }
            category.setName(request.getName());
        }

        category = categoryRepository.save(category);
        return CategoryDTO.Response.fromEntity(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this category");
        }

        categoryRepository.delete(category);
    }
}
