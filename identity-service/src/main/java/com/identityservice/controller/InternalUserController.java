package com.identityservice.controller;

import com.identityservice.dto.response.UserContactResponse;
import com.identityservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @GetMapping("/{userId}/contact")
    public UserContactResponse getUserContact(@PathVariable UUID userId) {
        return authService.getUserContact(userId);
    }
}
