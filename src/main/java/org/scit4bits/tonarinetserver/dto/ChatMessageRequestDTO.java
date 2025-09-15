package org.scit4bits.tonarinetserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 요청을 위한 DTO
 */
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

    @Schema(description = "Message type", example = "CHAT", allowableValues = {"CHAT", "JOIN", "LEAVE"})
    @Builder.Default
    private MessageType type = MessageType.CHAT;

    /**
     * 메시지 타입 열거형
     */
    public enum MessageType {
        /** 일반 채팅 메시지 */
        CHAT,
        /** 사용자가 방에 입장 */
        JOIN,
        /** 사용자가 방을 나감 */
        LEAVE
    }
}
