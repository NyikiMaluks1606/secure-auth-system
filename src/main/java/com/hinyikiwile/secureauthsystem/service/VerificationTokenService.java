package com.hinyikiwile.secureauthsystem.service;

import com.hinyikiwile.secureauthsystem.entity.User;
import com.hinyikiwile.secureauthsystem.entity.VerificationToken;
import com.hinyikiwile.secureauthsystem.repository.UserRepository;
import com.hinyikiwile.secureauthsystem.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.UUID;


@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    private final UserRepository userRepository;

    @Value("${verification.expiration}")
    private Long verificationExpiration;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository,
                                    UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    public VerificationToken createVerificationToken(User user) {

        verificationTokenRepository.findByUser(user)
                .ifPresent(verificationTokenRepository::delete);

        verificationTokenRepository.flush();

        VerificationToken verificationToken = new VerificationToken();

        verificationToken.setUser(user);

        verificationToken.setToken(
        UUID.randomUUID().toString());

        verificationToken.setExpiryDate(
                Instant.now().plusMillis(verificationExpiration));

        return verificationTokenRepository.save(verificationToken);
    }

    public void verifyToken(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Invalid verification token."));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token has expired.");
        }

        User user = verificationToken.getUser();

        user.setEnabled(true);

        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }
}
