package com.hinyikiwile.secureauthsystem.controller;

import com.hinyikiwile.secureauthsystem.dto.RegisterRequest;
import com.hinyikiwile.secureauthsystem.dto.RegisterResponse;
import com.hinyikiwile.secureauthsystem.dto.LoginRequest;
import com.hinyikiwile.secureauthsystem.dto.LoginResponse;
import com.hinyikiwile.secureauthsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        userService.registerUser(request);

        RegisterResponse response =
                new RegisterResponse("User registered successfully!");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Authenticate user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        String token = userService.loginUser(request);

        LoginResponse response = new LoginResponse(token);

        return ResponseEntity.ok(response);
    }
}
