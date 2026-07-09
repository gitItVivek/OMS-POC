package com.identityservice.security;

import com.identityservice.dal.RevokedTokenDal;
import com.identityservice.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final RevokedTokenDal revokedTokenDal;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isEmpty()) {
                try {
                    String tokenHash = tokenService.hashToken(token);
                    if (revokedTokenDal.existsByTokenHash(tokenHash)) {
                        log.debug("Rejected revoked token for {}", request.getRequestURI());
                    } else {
                        IdentityUserPrincipal principal = tokenService.parseToken(token);
                        var auth = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (ExpiredJwtException ex) {
                    log.debug("Expired JWT for {}: {}", request.getRequestURI(), ex.getMessage());
                } catch (JwtException ex) {
                    log.warn("Invalid JWT for {}: {}", request.getRequestURI(), ex.getMessage());
                } catch (Exception ex) {
                    log.error("JWT authentication failed for {}", request.getRequestURI(), ex);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
