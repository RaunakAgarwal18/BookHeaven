package com.bookheaven.book_service.config;


import com.bookheaven.book_service.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/book/seller").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/book", "/api/book/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/book/bulk").hasRole("SYSTEM")
                        .requestMatchers(HttpMethod.POST, "/api/book/internal/**").hasRole("SYSTEM")
                        .requestMatchers(HttpMethod.POST, "/api/book/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/book/reduce-stock").hasRole("SYSTEM")
                        .requestMatchers(HttpMethod.PUT, "/api/book/restore-stock").hasRole("SYSTEM")
                        .requestMatchers(HttpMethod.PUT, "/api/book/internal/**").hasRole("SYSTEM")
                        .requestMatchers(HttpMethod.PUT, "/api/book/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/book/**").hasAnyRole("SELLER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}