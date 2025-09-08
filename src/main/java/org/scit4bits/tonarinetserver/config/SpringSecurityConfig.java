package org.scit4bits.tonarinetserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(request -> {
                            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                            config.setAllowedOrigins(java.util.List.of("*"));
                            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            config.setAllowedHeaders(java.util.List.of("*", "Authorization"));
                            config.setExposedHeaders(java.util.List.of("Content-Disposition"));
                            config.setAllowCredentials(false);
                            return config;
                        }))
                .csrf(csrf -> csrf.disable()) // REST API Server doesn't need CSRF protection
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll() // Allow auth endpoints
                        .requestMatchers("/ws/**").permitAll() // Allow WebSocket endpoints
                        .requestMatchers("/app/**").permitAll() // Allow STOMP destinations
                        .requestMatchers("/topic/**").permitAll() // Allow message broker endpoints
                        .requestMatchers("/queue/**").permitAll() // Allow message broker endpoints
                        .requestMatchers("/api/user/**").authenticated() // Secure this endpoint
                        .requestMatchers("/api/board/**").authenticated() // Secure board endpoints
                        .requestMatchers("/api/chat/**").authenticated() // Secure chat REST endpoints
                        .anyRequest().permitAll() // Allow other requests for now
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}