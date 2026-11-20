package com.hinyikiwile.secureauthsystem.dto;

import com.hinyikiwile.secureauthsystem.entity.Role;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean enabled;
    private final Role role;

    public UserProfileResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            boolean enabled,
            Role role) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.role = role;
    }
}
