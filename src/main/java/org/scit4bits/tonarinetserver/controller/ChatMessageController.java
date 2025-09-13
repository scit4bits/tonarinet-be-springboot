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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat Message", description = "Real-time chat messaging API")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final AIService aiService;

    /**
     * WebSocket endpoint for sending messages
     * Clients should send messages to /app/chat/send/{roomId}
     */
    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Integer roomId,
                            @Payload ChatMessageRequestDTO messageRequest,
                            UserPrincipal principal) {
        try {

            messageRequest.setChatroomId(roomId);

            log.debug("roomId {}, messageRequest {}, Principal {}", roomId, messageRequest,
                    principal.getName());

            // Send the message
            ChatMessageResponseDTO savedMessage = chatMessageService.sendMessage(messageRequest, principal.getId());

            // Broadcast the message to all subscribers of the chat room
            messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, savedMessage);

            log.info("Message sent to room {} by user {}", roomId, principal.getId());

            // background task create : AI response and send back to the room
            if (chatRoomService.checkIfAIChatroom(roomId)) {
                // send message to chatroom that notices AI response is being generated
                ChatMessageResponseDTO aiNoticeMessage = chatMessageService.sendMessage(
                        ChatMessageRequestDTO.builder()
                                .chatroomId(roomId)
                                .message("AI is generating a response...")
                                .build(),
                        0);

                messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, aiNoticeMessage);

                CompletableFuture.runAsync(() -> {
                    String aiResponse = aiService.generateResponseWithMemory(messageRequest.getMessage(), roomId);
                    ChatMessageResponseDTO aiMessage = chatMessageService.sendMessage(
                            ChatMessageRequestDTO.builder()
                                    .chatroomId(roomId)
                                    .message(aiResponse)
                                    .build(),
                            0);
                    messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, aiMessage);
                });
            }
        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            // Send error message back to the senderd
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors",
                    "Failed to send message: " + e.getMessage());
        }
    }

    /**
     * REST endpoint to get chat history
     */
    @GetMapping("/room/{roomId}/messages")
    @Operation(summary = "Get chat messages for a room", security = @SecurityRequirement(name = "bearerAuth"))
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
     * REST endpoint to get all messages in a chat room
     */
    @GetMapping("/room/{roomId}/messages/all")
    @Operation(summary = "Get all chat messages for a room", security = @SecurityRequirement(name = "bearerAuth"))
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
     * REST endpoint to send a message (alternative to WebSocket)
     */
    @PostMapping("/room/{roomId}/send")
    @Operation(summary = "Send a message to a chat room", security = @SecurityRequirement(name = "bearerAuth"))
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

            // Also broadcast via WebSocket if available
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
     * REST endpoint to mark messages as read
     */
    @PostMapping("/room/{roomId}/read")
    @Operation(summary = "Mark messages as read", security = @SecurityRequirement(name = "bearerAuth"))
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
     * REST endpoint to delete a message
     */
    @DeleteMapping("/message/{messageId}")
    @Operation(summary = "Delete a chat message", security = @SecurityRequirement(name = "bearerAuth"))
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
