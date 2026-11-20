package com.hinyikiwile.secureauthsystem.service;

import com.hinyikiwile.secureauthsystem.dto.LoginResponse;
import com.hinyikiwile.secureauthsystem.entity.RefreshToken;
import com.hinyikiwile.secureauthsystem.entity.User;
import com.hinyikiwile.secureauthsystem.entity.VerificationToken;
import com.hinyikiwile.secureauthsystem.repository.UserRepository;
import com.hinyikiwile.secureauthsystem.dto.RegisterRequest;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hinyikiwile.secureauthsystem.dto.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;
import com.hinyikiwile.secureauthsystem.jwt.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.hinyikiwile.secureauthsystem.entity.Role;
import org.springframework.transaction.annotation.Transactional;
import com.hinyikiwile.secureauthsystem.repository.RefreshTokenRepository;
import com.hinyikiwile.secureauthsystem.repository.VerificationTokenRepository;


import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       VerificationTokenService verificationTokenService,
                       EmailService emailService,
                       RefreshTokenRepository refreshTokenRepository,
                       VerificationTokenRepository verificationTokenRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.verificationTokenService = verificationTokenService;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public User registerUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        VerificationToken verificationToken =
                verificationTokenService.createVerificationToken(savedUser);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                verificationToken.getToken()
        );
        return savedUser;
    }

    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword());

        if (!matches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new BadCredentialsException(
                    "Please verify your email before logging in.");
        }

        String accessToken = jwtService.generateToken(user.getEmail());

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUserRole(Long id, Role newRole) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found."));

        user.setRole(newRole);

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found."));

        refreshTokenRepository.deleteByUser(user);
        verificationTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}
