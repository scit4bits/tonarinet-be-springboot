package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.ChatRoomRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatRoomResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
@Tag(name = "Chat Room", description = "Chat room management API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    @Operation(summary = "Create a new chat room", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChatRoomResponseDTO> createChatRoom(
            @Valid @RequestBody ChatRoomRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ChatRoomResponseDTO chatRoom = chatRoomService.createChatRoom(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
        } catch (Exception e) {
            log.error("Error creating chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all chat rooms", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatRoomResponseDTO>> getAllChatRooms(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only admin can see all chat rooms
        if (!user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.getAllChatRooms();
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error fetching chat rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get chat room by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChatRoomResponseDTO> getChatRoomById(@PathVariable Integer id, @AuthenticationPrincipal User user) {
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

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get chat rooms by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRoomsByUserId(
            @PathVariable Integer userId, 
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Users can only see their own chat rooms, unless they're admin
        if (!user.getId().equals(userId) && !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.getChatRoomsByUserId(userId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error fetching chat rooms by user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's chat rooms", security = @SecurityRequirement(name = "bearerAuth"))
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

    @GetMapping("/leader/{leaderId}")
    @Operation(summary = "Get chat rooms by leader ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChatRoomResponseDTO>> getChatRoomsByLeaderId(
            @PathVariable Integer leaderId, 
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Users can only see their own led chat rooms, unless they're admin
        if (!user.getId().equals(leaderId) && !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.getChatRoomsByLeaderId(leaderId);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error fetching chat rooms by leader: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a chat room", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChatRoomResponseDTO> updateChatRoom(
            @PathVariable Integer id,
            @Valid @RequestBody ChatRoomRequestDTO request,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            ChatRoomResponseDTO chatRoom = chatRoomService.updateChatRoom(id, request, user);
            return ResponseEntity.ok(chatRoom);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("Only the chat room leader") || 
                      e.getMessage().contains("admin")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error updating chat room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a chat room", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> deleteChatRoom(
            @PathVariable Integer id,
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

    @PostMapping("/{id}/join")
    @Operation(summary = "Join a chat room", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> joinChatRoom(
            @PathVariable Integer id,
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

    @PostMapping("/{id}/leave")
    @Operation(summary = "Leave a chat room", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SimpleResponse> leaveChatRoom(
            @PathVariable Integer id,
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

    @GetMapping("/search")
    @Operation(summary = "Search chat rooms")
    public ResponseEntity<PagedResponse<ChatRoomResponseDTO>> searchChatRooms(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        try {
            PagedResponse<ChatRoomResponseDTO> chatRooms = chatRoomService.searchChatRooms(
                searchBy, search, page, pageSize, sortBy, sortDirection);
            return ResponseEntity.ok(chatRooms);
        } catch (Exception e) {
            log.error("Error searching chat rooms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
