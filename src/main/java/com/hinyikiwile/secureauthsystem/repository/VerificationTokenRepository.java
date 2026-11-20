package com.hinyikiwile.secureauthsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hinyikiwile.secureauthsystem.entity.VerificationToken;
import com.hinyikiwile.secureauthsystem.entity.User;

import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    void deleteByUser(User user);
}
