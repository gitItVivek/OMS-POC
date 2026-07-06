package com.orderservice.client;

import com.orderservice.dto.IdentityUserResponseDto;
import com.orderservice.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityServiceClient {

    private final RestClient identityRestClient;

    public UUID resolveUserId(String authorizationHeader) {
        String bearerToken = extractBearerToken(authorizationHeader);
        try {
            IdentityUserResponseDto user = identityRestClient.get()
                    .uri("/api/auth/me")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(IdentityUserResponseDto.class);
            if (user == null || user.id() == null) {
                throw new UnauthorizedException("Identity service returned an empty user profile");
            }
            return user.id();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Identity rejected token: {}", e.getResponseBodyAsString());
            throw new UnauthorizedException(
                    "Token rejected by identity-service. Login again and use a single Authorization header in Postman.");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new UnauthorizedException("Account is not allowed to access this resource");
        } catch (HttpClientErrorException e) {
            throw new UnauthorizedException("Identity service error (" + e.getStatusCode().value() + ")");
        } catch (RestClientException e) {
            log.error("Identity service call failed: {}", e.getMessage(), e);
            throw new UnauthorizedException(
                    "Identity service call failed. Check identity.service.base-url (use 127.0.0.1:8086 on Windows; "
                            + "if order-service runs in WSL while identity runs on Windows, use the Windows host IP). "
                            + "Cause: " + e.getMessage());
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authentication required. Login via identity-service.");
        }
        String header = authorizationHeader.trim();
        if (header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }
            if (token.isEmpty()) {
                throw new UnauthorizedException("Authentication required. Login via identity-service.");
            }
            return token;
        }
        throw new UnauthorizedException("Authorization header must be: Bearer <token>");
    }
}
