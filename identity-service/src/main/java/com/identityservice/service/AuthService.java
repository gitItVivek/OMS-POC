package com.identityservice.service;

import com.identityservice.config.JwtProperties;
import com.identityservice.dal.RevokedTokenDal;
import com.identityservice.dal.UserDal;
import com.identityservice.dto.request.LoginEmailRequest;
import com.identityservice.dto.request.RegisterEmailRequest;
import com.identityservice.dto.response.AuthResponse;
import com.identityservice.dto.response.UserSummaryResponse;
import com.identityservice.entity.RevokedToken;
import com.identityservice.entity.User;
import com.identityservice.enums.UserStatus;
import com.identityservice.exception.AccountNotActiveException;
import com.identityservice.exception.EmailAlreadyExistsException;
import com.identityservice.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDal userDal;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final RevokedTokenDal revokedTokenDal;

    @Transactional
    public AuthResponse registerWithEmail(RegisterEmailRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userDal.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName().trim())
                .isVerified(false)
                .build();
        user = userDal.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse loginWithEmail(LoginEmailRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userDal.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException();
        }
        return buildAuthResponse(user);
    }

    public void logoutAndTokenBlackListing(String rawJwt) {
        if (rawJwt == null || rawJwt.isBlank()) {
            throw new InvalidCredentialsException();
        }
        String token = rawJwt.trim();
        String hash = tokenService.hashToken(token);
        if (revokedTokenDal.existsByTokenHash(hash)) {
            return;
        }
        Instant expiresAt = tokenService.getTokenExpiration(token);
        revokedTokenDal.save(
                RevokedToken.builder()
                        .tokenHash(hash)
                        .expiresAt(expiresAt)
                        .build()
        );
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                tokenService.generateToken(user),
                jwtProperties.getTokenType(),
                jwtProperties.getExpirySeconds(),
                toSummary(user)
        );
    }

    private UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getIsVerified()
        );
    }
}
