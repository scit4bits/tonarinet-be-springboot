package org.scit4bits.tonarinetserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the greeting messages
        // back to the client
        // on destinations prefixed with "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue");

        // Designate the "/app" prefix for messages that are bound for @MessageMapping
        // methods
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options so that
        // alternate transports may be used if WebSocket is not available
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback

        // Also register without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Get Authorization header from STOMP headers
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        log.warn("Missing or invalid Authorization header in WebSocket connection");
                        throw new RuntimeException("Missing or invalid authorization header");
                    }

                    try {
                        String jwt = authHeader.substring(7);
                        String userId = jwtService.extractUserId(jwt);

                        if (userId != null && jwtService.validateToken(jwt)) {
                            User user = userRepository.findById(Integer.parseInt(userId)).orElse(null);

                            if (user != null) {
                                UserPrincipal userPrincipal = new UserPrincipal(user.getId());
                                accessor.setUser(userPrincipal);
                                log.debug("WebSocket user authenticated: {} {}", user.getId(), user.getEmail());
                            } else {
                                log.warn("User not found for ID: {}", userId);
                                throw new RuntimeException("User not found");
                            }
                        } else {
                            log.warn("Invalid JWT token in WebSocket connection");
                            throw new RuntimeException("Invalid authorization token");
                        }
                    } catch (NumberFormatException e) {
                        log.error("Invalid user ID in JWT: {}", e.getMessage());
                        throw new RuntimeException("Invalid user ID in token");
                    } catch (Exception e) {
                        log.error("WebSocket authentication error: {}", e.getMessage());
                        throw new RuntimeException("Authentication failed");
                    }
                }

                return message;
            }
        });
    }

    // UserPrincipal class implementing Principal for WebSocket authentication
    public static class UserPrincipal implements Principal {
        private final Integer id;

        public UserPrincipal(Integer id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return String.valueOf(id);
        }

        public Integer getId() {
            return id;
        }
    }
}
