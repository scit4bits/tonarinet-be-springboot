package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.config.WebSocketConfig.UserPrincipal;
import org.scit4bits.tonarinetserver.dto.ChatMessageRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatMessageResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.service.AIService;
import org.scit4bits.tonarinetserver.service.ChatMessageService;
import org.scit4bits.tonarinetserver.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 실시간 채팅 메시지 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat Message", description = "실시간 채팅 메시지 API")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final AIService aiService;

    /**
     * WebSocket을 통해 메시지를 전송하는 엔드포인트입니다.
     * 클라이언트는 /app/chat/send/{roomId}로 메시지를 보내야 합니다.
     * @param roomId 채팅방 ID
     * @param messageRequest 메시지 요청 정보
     * @param principal 현재 로그인한 사용자 정보
     */
    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Integer roomId,
                            @Payload ChatMessageRequestDTO messageRequest,
                            UserPrincipal principal) {
        try {

            messageRequest.setChatroomId(roomId);

            log.debug("roomId {}, messageRequest {}, Principal {}", roomId, messageRequest,
                    principal.getName());

            // 메시지를 저장하고 저장된 메시지를 반환받습니다.
            ChatMessageResponseDTO savedMessage = chatMessageService.sendMessage(messageRequest, principal.getId());

            log.debug("Saved message: {}", savedMessage);

            // 해당 채팅방을 구독하는 모든 클라이언트에게 메시지를 브로드캐스트합니다.
            messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, savedMessage);

            log.info("Message sent to room {} by user {}", roomId, principal.getId());

            // AI 채팅방인 경우, AI 응답을 생성하여 전송합니다.
            if (chatRoomService.checkIfAIChatroom(roomId)) {
                // AI가 응답을 생성 중이라는 알림 메시지를 보냅니다.
                ChatMessageResponseDTO aiNoticeMessage = chatMessageService.sendMessage(
                        ChatMessageRequestDTO.builder()
                                .chatroomId(roomId)
                                .message("AI가 응답을 생성 중입니다...")
                                .build(),
                        0); // AI 응답은 user 0 (system)으로 처리합니다.

                messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, aiNoticeMessage);

                // 비동기적으로 AI 응답을 생성하고 전송합니다.
                CompletableFuture.runAsync(() -> {
                    String aiResponse = aiService.generateResponseWithMemory(messageRequest.getMessage(), roomId);
                    ChatMessageResponseDTO aiMessage = chatMessageService.sendMessage(
                            ChatMessageRequestDTO.builder()
                                    .chatroomId(roomId)
                                    .message(aiResponse)
                                    .build(),
                            0); // AI 응답은 user 0 (system)으로 처리합니다.
                    messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, aiMessage);
                });
            }
        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            // 메시지 전송 실패 시, 해당 사용자에게 에러 메시지를 전송합니다.
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors",
                    "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * REST API를 통해 채팅 내역을 조회합니다.
     * @param roomId 채팅방 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param user 현재 로그인한 사용자 정보
     * @return 페이징 처리된 ChatMessageResponseDTO 리스트
     */
    @GetMapping("/room/{roomId}/messages")
    @Operation(summary = "채팅방 메시지 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatMessageResponseDTO>> getMessages(
            @PathVariable("roomId") Integer roomId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "50") Integer size,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ChatMessageResponseDTO> messages = chatMessageService.getMessagesByChatRoom(roomId, page, size, user);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Error fetching messages for room {}: {}", roomId, e.getMessage());
            if (e.getMessage().contains("not a member")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching messages for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * REST API를 통해 채팅방의 모든 메시지를 조회합니다.
     * @param roomId 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return ChatMessageResponseDTO 리스트
     */
    @GetMapping("/room/{roomId}/messages/all")
    @Operation(summary = "채팅방의 모든 메시지 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatMessageResponseDTO>> getAllMessages(
            @PathVariable("roomId") Integer roomId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ChatMessageResponseDTO> messages = chatMessageService.getAllMessagesByChatRoom(roomId, user);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            log.error("Error fetching all messages for room {}: {}", roomId, e.getMessage());
            if (e.getMessage().contains("not a member")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching all messages for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * REST API를 통해 메시지를 전송합니다. (WebSocket 대안)
     * @param roomId 채팅방 ID
     * @param messageRequest 메시지 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 전송된 ChatMessageResponseDTO 정보
     */
    @PostMapping("/room/{roomId}/send")
    @Operation(summary = "채팅방에 메시지 전송", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChatMessageResponseDTO> sendMessageRest(
            @PathVariable("roomId") Integer roomId,
            @Valid @RequestBody ChatMessageRequestDTO messageRequest,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            messageRequest.setChatroomId(roomId);
            ChatMessageResponseDTO savedMessage = chatMessageService.sendMessage(messageRequest, user.getId());

            // WebSocket을 통해서도 메시지를 브로드캐스트합니다.
            try {
                messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, savedMessage);
            } catch (Exception e) {
                log.warn("Failed to broadcast message via WebSocket: {}", e.getMessage());
            }

            return ResponseEntity.ok(savedMessage);
        } catch (RuntimeException e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            if (e.getMessage().contains("not a member")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 메시지를 읽음으로 표시합니다.
     * @param roomId 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/room/{roomId}/read")
    @Operation(summary = "메시지 읽음 처리", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> markAsRead(
            @PathVariable("roomId") Integer roomId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            chatMessageService.markMessagesAsRead(roomId, user);
            return ResponseEntity.ok(new SimpleResponse("Messages marked as read"));
        } catch (RuntimeException e) {
            log.error("Error marking messages as read for room {}: {}", roomId, e.getMessage());
            if (e.getMessage().contains("not a member")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error marking messages as read for room {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 메시지를 삭제합니다.
     * @param messageId 삭제할 메시지 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "채팅 메시지 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteMessage(
            @PathVariable("messageId") Integer messageId,
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            chatMessageService.deleteMessage(messageId, user);
            return ResponseEntity.ok(new SimpleResponse("Message deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting message {}: {}", messageId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the sender") || e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting message {}: {}", messageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
