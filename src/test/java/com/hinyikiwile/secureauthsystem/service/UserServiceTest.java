package com.hinyikiwile.secureauthsystem.service;

import com.hinyikiwile.secureauthsystem.dto.LoginResponse;
import com.hinyikiwile.secureauthsystem.entity.RefreshToken;

import com.hinyikiwile.secureauthsystem.jwt.JwtService;
import com.hinyikiwile.secureauthsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hinyikiwile.secureauthsystem.dto.RegisterRequest;
import com.hinyikiwile.secureauthsystem.entity.Role;
import com.hinyikiwile.secureauthsystem.entity.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import com.hinyikiwile.secureauthsystem.dto.LoginRequest;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        RegisterRequest request = new RegisterRequest();

        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encryptedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User savedUser = userService.registerUser(request);

        // Assert
        assertNotNull(savedUser);

        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("john@gmail.com", savedUser.getEmail());
        assertEquals("encryptedPassword", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());

        verify(userRepository).existsByEmail("john@gmail.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        RegisterRequest request = new RegisterRequest();

        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(request)
        );

        assertEquals(
                "Email is already registered.",
                exception.getMessage()
        );

        verify(userRepository).existsByEmail("john@gmail.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest(
                "john@gmail.com",
                "password123"
        );

        User user = new User();
        user.setEmail("john@gmail.com");
        user.setPassword("encryptedPassword");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encryptedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken("john@gmail.com"))
                .thenReturn("jwt-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);

        // Act
        LoginResponse response = userService.loginUser(request);

        // Assert
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(userRepository).findByEmail("john@gmail.com");
        verify(passwordEncoder)
                .matches("password123", "encryptedPassword");
        verify(jwtService).generateToken("john@gmail.com");
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "john@gmail.com",
                "wrongPassword"
        );

        User user = new User();
        user.setEmail("john@gmail.com");
        user.setPassword("encryptedPassword");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encryptedPassword"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () ->
                userService.loginUser(request)
        );

        // Verify interactions
        verify(userRepository).findByEmail("john@gmail.com");
        verify(passwordEncoder).matches("wrongPassword", "encryptedPassword");

        // Ensure token generation is never reached
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "nonexistent@gmail.com",
                "password123"
        );

        when(userRepository.findByEmail("nonexistent@gmail.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> userService.loginUser(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        // Verify repository was called
        verify(userRepository).findByEmail("nonexistent@gmail.com");

        // Ensure downstream dependencies are never called
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }
}
