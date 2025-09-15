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

/**
 * Spring Security 설정을 구성하는 클래스
 */
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${swagger.auth.username}")
    private String swaggerUsername;
    @Value("${swagger.auth.password}")
    private String swaggerPassword;
    /**
     * 보안 필터 체인을 설정합니다.
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 보안 설정 중 예외 발생 시
     */
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
                .csrf(csrf -> csrf.disable()) // REST API 서버는 CSRF 보호가 필요하지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않음
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").hasRole("SWAGGER_USER")
                        .requestMatchers("/api/auth/**").permitAll() // 인증 관련 엔드포인트는 모두 허용
                        .requestMatchers("/ws/**").permitAll() // 웹소켓 엔드포인트 허용
                        .requestMatchers("/app/**").permitAll() // STOMP 목적지 허용
                        .requestMatchers("/topic/**").permitAll() // 메시지 브로커 엔드포인트 허용
                        .requestMatchers("/queue/**").permitAll() // 메시지 브로커 엔드포인트 허용
                        .requestMatchers("/api/user/**").authenticated() // 사용자 관련 엔드포인트는 인증 필요
                        .requestMatchers("/api/board/**").authenticated() // 게시판 관련 엔드포인트는 인증 필요
                        .requestMatchers("/api/chat/**").authenticated() // 채팅 관련 REST 엔드포인트는 인증 필요
                        .anyRequest().permitAll() // 나머지 요청은 일단 모두 허용
                )
                .httpBasic(basic -> basic
                        .realmName("Swagger UI")
                ) // Enable HTTP Basic authentication for Swagger endpoints
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder를 빈으로 등록합니다.
     * @return BCryptPasswordEncoder 객체
     */
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

