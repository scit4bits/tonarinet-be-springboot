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

/**
 * WebSocket 및 STOMP 메시징을 설정하는 클래스
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * 메시지 브로커를 설정합니다.
     * @param config MessageBrokerRegistry
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // "/topic", "/queue" 접두사를 사용하는 간단한 메모리 기반 메시지 브로커를 활성화합니다.
        config.enableSimpleBroker("/topic", "/queue");

        // "/app" 접두사는 @MessageMapping 메서드로 바인딩되는 메시지를 위해 지정합니다.
        config.setApplicationDestinationPrefixes("/app");

        // 개인 메시지를 위한 사용자 목적지 접두사를 설정합니다.
        config.setUserDestinationPrefix("/user");
    }

    /**
     * STOMP 엔드포인트를 등록합니다.
     * @param registry StompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // "/ws" 엔드포인트를 등록하고, WebSocket을 사용할 수 없는 경우를 대비해 SockJS 대체 옵션을 활성화합니다.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 개발을 위해 모든 오리진 허용
                .withSockJS(); // SockJS 대체 활성화

        // 네이티브 WebSocket 클라이언트를 위해 SockJS 없이도 등록합니다.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 클라이언트 인바운드 채널을 설정하고 JWT 인증을 위한 인터셉터를 추가합니다.
     * @param registration ChannelRegistration
     */
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // CONNECT 명령어일 때만 인증 처리
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // STOMP 헤더에서 Authorization 헤더 가져오기
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        log.warn("웹소켓 연결에 Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
                        throw new RuntimeException("Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
                    }

                    try {
                        String jwt = authHeader.substring(7);
                        String userId = jwtService.extractUserId(jwt);

                        if (userId != null && jwtService.validateToken(jwt)) {
                            User user = userRepository.findById(Integer.parseInt(userId)).orElse(null);

                            if (user != null) {
                                UserPrincipal userPrincipal = new UserPrincipal(user.getId());
                                accessor.setUser(userPrincipal);
                                log.debug("웹소켓 사용자 인증 성공: {} {}", user.getId(), user.getEmail());
                            } else {
                                log.warn("사용자를 찾을 수 없습니다: {}", userId);
                                throw new RuntimeException("사용자를 찾을 수 없습니다.");
                            }
                        } else {
                            log.warn("웹소켓 연결에 사용된 JWT 토큰이 유효하지 않습니다.");
                            throw new RuntimeException("유효하지 않은 인증 토큰입니다.");
                        }
                    } catch (NumberFormatException e) {
                        log.error("JWT의 사용자 ID가 잘못되었습니다: {}", e.getMessage());
                        throw new RuntimeException("토큰의 사용자 ID가 잘못되었습니다.");
                    } catch (Exception e) {
                        log.error("웹소켓 인증 오류: {}", e.getMessage());
                        throw new RuntimeException("인증에 실패했습니다.");
                    }
                }

                return message;
            }
        });
    }

    /**
     * WebSocket 인증을 위한 Principal 구현 클래스
     */
    public static class UserPrincipal implements Principal {
        private final Integer id;

        /**
         * UserPrincipal 생성자
         * @param id 사용자 ID
         */
        public UserPrincipal(Integer id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return String.valueOf(id);
        }

        /**
         * 사용자 ID를 반환합니다.
         * @return 사용자 ID
         */
        public Integer getId() {
            return id;
        }
    }
}
