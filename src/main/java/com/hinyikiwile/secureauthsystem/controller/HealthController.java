package com.hinyikiwile.secureauthsystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HealthController {
    @GetMapping("/")
    public String home() {
        return "SecureAuth API is running!";
    }
    @GetMapping("/health")
    public String health() {
        return  "Application Status: OK";
    }
}
