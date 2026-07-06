package com.identityservice.controller;

import com.identityservice.dto.request.LoginEmailRequest;
import com.identityservice.dto.request.RegisterEmailRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.UserSummaryResponse;
import com.identityservice.exception.InvalidCredentialsException;
import com.identityservice.security.IdentityUserPrincipal;
import com.identityservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterEmailRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerWithEmail(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginEmailRequest req) {
        return ResponseEntity.ok(authService.loginWithEmail(req));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof IdentityUserPrincipal principal)) {
            throw new InvalidCredentialsException();
        }
        return ResponseEntity.ok(authService.getUserSummary(principal.userId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }
        authService.logoutAndTokenBlackListing(authorization.substring(7));
        return ResponseEntity.noContent().build();
    }
}
