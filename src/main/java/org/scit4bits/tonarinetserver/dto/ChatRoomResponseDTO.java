package org.scit4bits.tonarinetserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅방 응답을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat room response DTO")
public class ChatRoomResponseDTO {

    @Schema(description = "Chat room ID", example = "1")
    private Integer id;

    @Schema(description = "Chat room title", example = "Weekly Team Chat")
    private String title;

    @Schema(description = "Chat room description", example = "Weekly team discussion room")
    private String description;

    @Schema(description = "Whether the room should remain active", example = "false")
    private Boolean forceRemain;

    @Schema(description = "Creation date and time", example = "2024-01-01T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Leader user ID", example = "1")
    private Integer leaderUserId;

    @Schema(description = "Leader user information")
    private UserDTO leaderUser;

    @Schema(description = "List of users in the chat room")
    private List<UserDTO> users;

    @Schema(description = "Number of users in the chat room", example = "5")
    private Integer userCount;

    @Schema(description = "Number of messages in the chat room", example = "25")
    private Integer messageCount;

    /**
     * ChatRoom 엔티티를 ChatRoomResponseDTO로 변환합니다.
     * @param chatRoom 변환할 ChatRoom 엔티티
     * @return 변환된 ChatRoomResponseDTO 객체
     */
    public static ChatRoomResponseDTO fromEntity(ChatRoom chatRoom) {
        ChatRoomResponseDTOBuilder builder = ChatRoomResponseDTO.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .description(chatRoom.getDescription())
                .forceRemain(chatRoom.getForceRemain())
                .createdAt(chatRoom.getCreatedAt())
                .leaderUserId(chatRoom.getLeaderUserId());

        // 방장 사용자 정보가 있는 경우 추가
        if (chatRoom.getLeaderUser() != null) {
            builder.leaderUser(UserDTO.fromEntity(chatRoom.getLeaderUser()));
        }

        // 사용자 목록이 있는 경우 추가
        if (chatRoom.getUsers() != null) {
            List<UserDTO> userDTOs = chatRoom.getUsers().stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());
            builder.users(userDTOs);
            builder.userCount(userDTOs.size());
        } else {
            builder.userCount(0);
        }

        // 메시지 수가 있는 경우 추가
        if (chatRoom.getMessages() != null) {
            builder.messageCount(chatRoom.getMessages().size());
        } else {
            builder.messageCount(0);
        }

        return builder.build();
    }
}

