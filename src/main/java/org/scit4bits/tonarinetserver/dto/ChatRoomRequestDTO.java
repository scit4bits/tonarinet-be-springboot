package org.scit4bits.tonarinetserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat room request DTO")
public class ChatRoomRequestDTO {

    @Schema(description = "Chat room title", example = "Weekly Team Chat")
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Chat room description", example = "Weekly team discussion room")
    private String description;

    @Schema(description = "Whether the room should remain active", example = "false")
    @NotNull(message = "Force remain is required")
    @Builder.Default
    private Boolean forceRemain = false;

    @Schema(description = "List of user IDs to add to the chat room")
    private List<Integer> userIds;
}
