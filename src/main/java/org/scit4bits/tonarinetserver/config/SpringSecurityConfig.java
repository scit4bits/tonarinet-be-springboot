package org.scit4bits.tonarinetserver.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${swagger.auth.username}")
    private String swaggerUsername;
    @Value("${swagger.auth.password}")
    private String swaggerPassword;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(request -> {
                            org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                            config.setAllowedOrigins(java.util.List.of("*"));
                            config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                            config.setAllowedHeaders(java.util.List.of("*", "Authorization"));
                            config.setExposedHeaders(java.util.List.of("Content-Disposition"));
                            config.setAllowCredentials(false);
                            return config;
                        }))
                .csrf(csrf -> csrf.disable()) // REST API Server doesn't need CSRF protection
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Swagger UI endpoints require Basic Auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").hasRole("SWAGGER_USER")
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
                .httpBasic(basic -> basic
                        .realmName("Swagger UI")
                ) // Enable HTTP Basic authentication for Swagger endpoints
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails swaggerUser = User.builder()
                .username("swagger")
                .password(passwordEncoder().encode("swagger123"))
                .roles("SWAGGER_USER")
                .build();

        return new InMemoryUserDetailsManager(swaggerUser);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}