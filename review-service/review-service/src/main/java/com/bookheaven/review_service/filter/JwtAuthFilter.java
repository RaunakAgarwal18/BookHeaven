package com.bookheaven.review_service.filter;

import com.bookheaven.review_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    @Value("${gateway.communication.secret}")
    private String expectedGatewaySecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
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

        try {
            UUID userId = jwtUtil.extractId(token);
            String role = jwtUtil.extractRole(token);

            List<GrantedAuthority> authorities = Collections.emptyList();
            if (role != null) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            authorities
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // Invalid or expired token — treat as unauthenticated
            // Security rules will handle the 401
        }

        filterChain.doFilter(request, response);
    }
}