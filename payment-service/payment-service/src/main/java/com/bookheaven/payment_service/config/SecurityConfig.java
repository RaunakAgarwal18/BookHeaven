package com.bookheaven.payment_service.config;

import com.bookheaven.payment_service.filter.JwtAuthFilter;
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
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter filter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/payment/webhook").permitAll()
                        .requestMatchers("/api/payment/config").permitAll()
                        .requestMatchers("/api/payment/initiate").hasRole("SYSTEM")
                        .requestMatchers("/api/payment/refund").hasRole("SYSTEM")
                        .requestMatchers("/api/payment/admin/**").hasAnyRole("SYSTEM", "ADMIN")
                        .requestMatchers("/api/payment/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
