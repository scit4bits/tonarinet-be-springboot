package org.scit4bits.tonarinetserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat message request DTO")
public class ChatMessageRequestDTO {

    @Schema(description = "Chat room ID", example = "1")
    @NotNull(message = "Chat room ID is required")
    private Integer chatroomId;

    @Schema(description = "Message content", example = "Hello everyone!")
    @NotBlank(message = "Message content is required")
    private String message;

    @Schema(description = "Message type", example = "CHAT", allowableValues = { "CHAT", "JOIN", "LEAVE" })
    @Builder.Default
    private MessageType type = MessageType.CHAT;

    public enum MessageType {
        CHAT, // Regular chat message
        JOIN, // User joined the room
        LEAVE // User left the room
    }
}
