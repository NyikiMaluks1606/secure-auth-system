package com.hinyikiwile.secureauthsystem.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(
            String email,
            String firstName,
            String token) {
        String verificationUrl = "http://localhost:8081/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Please verify your email address");

        message.setText( "Hi " + firstName + ",\n\n"
                        + "Thank you for registering an account. "
                        + "Please click the link below to verify your account:\n\n"
                        + verificationUrl + "\n\n"
                        + "If you did not register for an account, "
                        + "you can safely ignore this email."
        );
        mailSender.send(message);
    }
}
