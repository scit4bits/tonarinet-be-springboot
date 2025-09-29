package org.scit4bits.tonarinetserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.ChatMessage;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답을 위한 DTO
 */
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

    @Schema(description = "Sender nickname", example = "john_doe")
    private String senderNickname; // 직접 주입

    @Schema(description = "Sender Profile File Id")
    private Integer senderProfileFileId; // 직접 주입


    @Schema(description = "Message content", example = "Hello everyone!")
    private String message;

    @Schema(description = "Message creation time", example = "2024-01-01T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Whether the message is read", example = "false")
    private Boolean isRead;

    @Schema(description = "Message type", example = "CHAT")
    private ChatMessageRequestDTO.MessageType type;

    /**
     * ChatMessage 엔티티를 ChatMessageResponseDTO로 변환합니다.
     * @param chatMessage 변환할 ChatMessage 엔티티
     * @return 변환된 ChatMessageResponseDTO 객체
     */
    public static ChatMessageResponseDTO fromEntity(ChatMessage chatMessage) {
        ChatMessageResponseDTOBuilder builder = ChatMessageResponseDTO.builder()
                .id(chatMessage.getId())
                .chatroomId(chatMessage.getChatroomId())
                .senderId(chatMessage.getSenderId())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .isRead(chatMessage.getIsRead())
                .type(ChatMessageRequestDTO.MessageType.CHAT); // Default to CHAT type

        // 발신자 사용자 정보가 있는 경우 추가
        if (chatMessage.getSender() != null) {
            builder.sender(UserDTO.fromEntity(chatMessage.getSender()));
        }

        return builder.build();
    }
}
