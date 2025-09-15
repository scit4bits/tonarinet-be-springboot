package org.scit4bits.tonarinetserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ChatRoomResponseDTO;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅방 관리 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
@Tag(name = "Chat Room", description = "채팅방 관리 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * ID로 특정 채팅방 정보를 조회합니다.
     * @param id 조회할 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return ChatRoomResponseDTO 형태의 채팅방 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "ID로 채팅방 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable("id") Integer id,
                                                               @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ChatRoomResponseDTO chatRoom = chatRoomService.getChatRoomById(id);
            return ResponseEntity.ok(chatRoom);
        } catch (RuntimeException e) {
            log.error("Error fetching chat room: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 현재 사용자가 속한 모든 채팅방 목록을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return ChatRoomResponseDTO 리스트
     */
    @GetMapping("/my")
    @Operation(summary = "현재 사용자의 채팅방 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatRoomResponseDTO>> getMyChatRooms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.getChatRoomsByUserId(user.getId());
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error fetching user's chat rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 채팅방을 삭제합니다.
     * @param id 삭제할 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "채팅방 삭제", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteChatRoom(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            chatRoomService.deleteChatRoom(id, user);
            return ResponseEntity.ok(new SimpleResponse("Chat room deleted successfully"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the chat room leader") ||
                    e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error deleting chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 채팅방에 참여합니다.
     * @param id 참여할 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/join")
    @Operation(summary = "채팅방 참여", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> joinChatRoom(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            chatRoomService.joinChatRoom(id, user);
            return ResponseEntity.ok(new SimpleResponse("Successfully joined chat room"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("already in")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error joining chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 특정 채팅방에서 나갑니다.
     * @param id 나갈 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return 성공 응답
     */
    @PostMapping("/{id}/leave")
    @Operation(summary = "채팅방 나가기", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> leaveChatRoom(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            chatRoomService.leaveChatRoom(id, user);
            return ResponseEntity.ok(new SimpleResponse("Successfully left chat room"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("leader cannot leave") ||
                    e.getMessage().contains("not in this chat room")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error leaving chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 읽지 않은 메시지 총 개수를 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return 읽지 않은 메시지 개수
     */
    @GetMapping("/unreadCount")
    public ResponseEntity<Integer> getUnreadMessagesCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            int unreadCount = chatRoomService.getUnreadMessagesCount(user.getId());
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            log.error("Error fetching unread messages count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
