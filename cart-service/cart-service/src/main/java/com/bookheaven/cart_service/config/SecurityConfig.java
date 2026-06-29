package com.bookheaven.cart_service.config;

import com.bookheaven.cart_service.filter.JwtAuthFilter;
import com.bookheaven.cart_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter filter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/cart/internal/**").hasRole("SYSTEM")
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/coupons").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/coupons/**").authenticated()
                        .requestMatchers("/api/coupons/**").hasAnyRole("SELLER", "ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
