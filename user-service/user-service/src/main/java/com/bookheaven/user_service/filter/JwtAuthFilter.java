package com.bookheaven.user_service.filter;

import com.bookheaven.user_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // your utility to parse/validate token

    @org.springframework.beans.factory.annotation.Value("${internal.service.secret}")
    private String internalServiceSecret;

    @org.springframework.beans.factory.annotation.Value("${gateway.communication.secret}")
    private String expectedGatewaySecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/user/auth") ||
                path.startsWith("/api/user/login") ||
                path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String serviceSecret = request.getHeader("X-Service-Secret");
        String gatewaySecret = request.getHeader("X-Gateway-Secret");

        boolean isInternalCall = serviceSecret != null && serviceSecret.equals(internalServiceSecret);
        boolean isGatewayCall = gatewaySecret != null && gatewaySecret.equals(expectedGatewaySecret);

        if (!isInternalCall && !isGatewayCall) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Direct access is blocked. Requests must go through the API Gateway.\"}");
            return;
        }

        if (isInternalCall) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring(7);
                    UUID userId = jwtUtil.extractId(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            AuthorityUtils.createAuthorityList("ROLE_SYSTEM")
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterChain.doFilter(request, response);
                    return;
                } catch (Exception e) {
                    // fallback to system user if token is invalid
                }
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    serviceSecret,
                    AuthorityUtils.createAuthorityList("ROLE_SYSTEM")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        UUID userId = jwtUtil.extractId(token);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                                userId,
                                                                token,
                                                                Collections.emptyList()
                                                            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}