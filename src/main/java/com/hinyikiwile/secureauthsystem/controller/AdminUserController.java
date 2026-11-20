package com.hinyikiwile.secureauthsystem.controller;

import com.hinyikiwile.secureauthsystem.dto.UpdateUserRoleRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hinyikiwile.secureauthsystem.dto.UserProfileResponse;
import com.hinyikiwile.secureauthsystem.entity.User;
import com.hinyikiwile.secureauthsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "View all registered users")
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(user -> new UserProfileResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.isEnabled(),
                        user.getRole()
                ))
                .toList();
    }

    @Operation(summary = "View a specific user")
    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse getUserById(@PathVariable Long id) {

        User user = userService.getUserById(id)
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

    @Operation(summary = "Update a user's role")
    @PutMapping("/api/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        User updatedUser = userService.updateUserRole( id, request.getRole() );
        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.isEnabled(),
                updatedUser.getRole() );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok("User deleted successfully.");
    }
}
