package com.hinyikiwile.secureauthsystem.service;

import com.hinyikiwile.secureauthsystem.jwt.JwtService;
import com.hinyikiwile.secureauthsystem.repository.RefreshTokenRepository;
import com.hinyikiwile.secureauthsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hinyikiwile.secureauthsystem.entity.RefreshToken;
import com.hinyikiwile.secureauthsystem.entity.User;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refreshExpiration}")
    private Long refreshExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public RefreshToken createRefreshToken(User user) {

        // Delete any existing refresh token for this user
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        refreshTokenRepository.flush();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);

        refreshToken.setToken(
                UUID.randomUUID().toString());

        refreshToken.setExpiryDate(
                Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(Instant.now())) {

            refreshTokenRepository.delete(token);

            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        return token;
    }

    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new RuntimeException("Refresh token not found."));
    }

    public String refreshAccessToken(String refreshToken) {

        RefreshToken token = findByToken(refreshToken);

        verifyExpiration(token);

        return jwtService.generateToken(
                token.getUser().getEmail()
        );
    }

    public void logout(String refreshToken) {

        RefreshToken token = findByToken(refreshToken);

        refreshTokenRepository.delete(token);
    }
}