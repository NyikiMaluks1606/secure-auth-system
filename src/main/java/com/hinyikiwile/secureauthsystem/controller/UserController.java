package com.hinyikiwile.secureauthsystem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class UserController {

    @Operation(summary = "View authenticated user profile")
    @GetMapping("/api/user/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String profile() {
        return "JWT authentication is working!";
    }
}