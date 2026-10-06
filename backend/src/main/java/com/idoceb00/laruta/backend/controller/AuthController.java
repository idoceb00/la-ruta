package com.idoceb00.laruta.backend.controller;

import com.idoceb00.laruta.backend.dto.CreateUserRequest;
import com.idoceb00.laruta.backend.dto.LoginRequest;
import com.idoceb00.laruta.backend.dto.LoginResponse;
import com.idoceb00.laruta.backend.dto.UserResponse;
import com.idoceb00.laruta.backend.service.AuthService;
import com.idoceb00.laruta.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
