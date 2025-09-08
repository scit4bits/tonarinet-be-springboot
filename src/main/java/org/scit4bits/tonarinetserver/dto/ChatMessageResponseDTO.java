package org.scit4bits.tonarinetserver.dto;

import java.time.LocalDateTime;

import org.scit4bits.tonarinetserver.entity.ChatMessage;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat message response DTO")
public class ChatMessageResponseDTO {

    @Schema(description = "Message ID", example = "1")
    private Integer id;

    @Schema(description = "Chat room ID", example = "1")
    private Integer chatroomId;

    @Schema(description = "Sender user ID", example = "1")
    private Integer senderId;

    @Schema(description = "Sender user information")
    private UserDTO sender;

    @Schema(description = "Message content", example = "Hello everyone!")
    private String message;

    @Schema(description = "Message creation time", example = "2024-01-01T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Whether the message is read", example = "false")
    private Boolean isRead;

    @Schema(description = "Message type", example = "CHAT")
    private ChatMessageRequestDTO.MessageType type;

    public static ChatMessageResponseDTO fromEntity(ChatMessage chatMessage) {
        ChatMessageResponseDTOBuilder builder = ChatMessageResponseDTO.builder()
                .id(chatMessage.getId())
                .chatroomId(chatMessage.getChatroomId())
                .senderId(chatMessage.getSenderId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .isRead(chatMessage.getIsRead())
                .type(ChatMessageRequestDTO.MessageType.CHAT); // Default to CHAT type

        // Add sender user info if available
        if (chatMessage.getSender() != null) {
            builder.sender(UserDTO.fromEntity(chatMessage.getSender()));
        }

        return builder.build();
    }
}
