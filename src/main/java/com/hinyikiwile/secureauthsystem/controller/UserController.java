package com.hinyikiwile.secureauthsystem.controller;

import com.hinyikiwile.secureauthsystem.entity.User;
import com.hinyikiwile.secureauthsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hinyikiwile.secureauthsystem.dto.UserProfileResponse;
import com.hinyikiwile.secureauthsystem.entity.User;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "View authenticated user profile")
    @GetMapping("/api/user/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserProfileResponse profile(Authentication authentication) {

        String email = authentication.getName();

        User user = userService.getUserByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found."));

        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRole()
        );
    }
}