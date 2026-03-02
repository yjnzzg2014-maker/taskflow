package com.taskflow.controller;

import com.taskflow.dto.UserDTO;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO.AuthResponse> register(@Valid @RequestBody UserDTO.RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO.AuthResponse> login(@Valid @RequestBody UserDTO.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDTO.AuthResponse> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO.Response> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}
