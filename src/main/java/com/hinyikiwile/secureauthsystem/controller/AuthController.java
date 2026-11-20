package com.hinyikiwile.secureauthsystem.controller;

import com.hinyikiwile.secureauthsystem.dto.*;
import com.hinyikiwile.secureauthsystem.service.RefreshTokenService;
import com.hinyikiwile.secureauthsystem.service.UserService;
import com.hinyikiwile.secureauthsystem.service.VerificationTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;


    public AuthController(UserService userService,
                          RefreshTokenService refreshTokenService,
                          VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenService = verificationTokenService;
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

    @Operation(summary = "Verify a user's email address")
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam("token") String token) {

        verificationTokenService.verifyToken(token);

        return ResponseEntity.ok("Email verified successfully!");
    }

    @Operation(summary = "Authenticate user and generate JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate a new access token using a refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        String accessToken =
                refreshTokenService.refreshAccessToken(
                        request.getRefreshToken());

        RefreshTokenResponse response =
                new RefreshTokenResponse(accessToken);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete token when user logs out")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logoutUser(
            @Valid @RequestBody LogoutRequest request) {
        refreshTokenService.logout(request.getRefreshToken());

        LogoutResponse response =
                new LogoutResponse("Logged out successfully!");

        return ResponseEntity.ok(response);
    }
}
