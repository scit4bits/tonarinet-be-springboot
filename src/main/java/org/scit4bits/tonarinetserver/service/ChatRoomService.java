package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import org.scit4bits.tonarinetserver.dto.ChatRoomRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatRoomResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.entity.ChatRoom;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserChatRoom;
import org.scit4bits.tonarinetserver.repository.ChatMessageRepository;
import org.scit4bits.tonarinetserver.repository.ChatRoomRepository;
import org.scit4bits.tonarinetserver.repository.UserChatRoomRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDTO, User currentUser) {
        // Create the chat room
        ChatRoom chatRoom = ChatRoom.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .forceRemain(requestDTO.getForceRemain() != null ? requestDTO.getForceRemain() : false)
                .leaderUserId(currentUser.getId())
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        // Add the creator to the chat room
        UserChatRoom creatorRelation = UserChatRoom.builder()
                .id(UserChatRoom.UserChatRoomId.builder()
                        .userId(currentUser.getId())
                        .chatroomId(chatRoom.getId())
                        .build())
                .user(currentUser)
                .chatroom(chatRoom)
                .build();
        userChatRoomRepository.save(creatorRelation);

        // Add other users to the chat room if specified
        if (requestDTO.getUserIds() != null && !requestDTO.getUserIds().isEmpty()) {
            for (Integer userId : requestDTO.getUserIds()) {
                if (!userId.equals(currentUser.getId())) { // Don't add creator twice
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

                    UserChatRoom userRelation = UserChatRoom.builder()
                            .id(UserChatRoom.UserChatRoomId.builder()
                                    .userId(userId)
                                    .chatroomId(chatRoom.getId())
                                    .build())
                            .user(user)
                            .chatroom(chatRoom)
                            .build();
                    userChatRoomRepository.save(userRelation);
                }
            }
        }

        // Fetch the complete chat room with relationships
        ChatRoom savedChatRoom = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow(() -> new RuntimeException("Chat room not found after creation"));

        return ChatRoomResponseDTO.fromEntity(savedChatRoom);
    }

    public boolean checkIfAIChatroom(Integer roomId) {
        ChatRoom chatroom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + roomId));
        return chatroom.getLeaderUserId().equals(0);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatRoomResponseDTO getChatRoomById(Integer id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + id));
        return ChatRoomResponseDTO.fromEntity(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getChatRoomsByUserId(Integer userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(userId);
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getChatRoomsByLeaderId(Integer leaderId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByLeaderUserId(leaderId);
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ChatRoomResponseDTO updateChatRoom(Integer id, ChatRoomRequestDTO requestDTO, User currentUser) {
        ChatRoom existingChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + id));

        // Check if user is the leader or admin
        if (!existingChatRoom.getLeaderUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the chat room leader or admin can update this chat room");
        }

        // Update chat room fields
        existingChatRoom.setTitle(requestDTO.getTitle());
        existingChatRoom.setDescription(requestDTO.getDescription());
        if (requestDTO.getForceRemain() != null) {
            existingChatRoom.setForceRemain(requestDTO.getForceRemain());
        }

        ChatRoom updatedChatRoom = chatRoomRepository.save(existingChatRoom);

        // Update user memberships if specified
        if (requestDTO.getUserIds() != null) {
            // Remove existing user relationships (except leader)
            userChatRoomRepository.deleteByChatroomIdAndUserIdNot(id, existingChatRoom.getLeaderUserId());

            // Add new user relationships
            for (Integer userId : requestDTO.getUserIds()) {
                if (!userId.equals(existingChatRoom.getLeaderUserId())) { // Don't add leader twice
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

                    UserChatRoom userRelation = UserChatRoom.builder()
                            .id(UserChatRoom.UserChatRoomId.builder()
                                    .userId(userId)
                                    .chatroomId(id)
                                    .build())
                            .user(user)
                            .chatroom(updatedChatRoom)
                            .build();
                    userChatRoomRepository.save(userRelation);
                }
            }
        }

        // Fetch the updated chat room with relationships
        ChatRoom finalChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat room not found after update"));

        return ChatRoomResponseDTO.fromEntity(finalChatRoom);
    }

    public void deleteChatRoom(Integer id, User currentUser) {
        ChatRoom existingChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + id));

        // Check if user is the leader or admin
        if (!existingChatRoom.getLeaderUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("Only the chat room leader or admin can delete this chat room");
        }

        // Delete related UserChatRoom entries first
        userChatRoomRepository.deleteByChatroomId(id);

        // Delete the chat room (messages will be cascade deleted)
        chatRoomRepository.deleteById(id);
    }

    public void joinChatRoom(Integer chatRoomId, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + chatRoomId));

        // Check if user is already in the chat room
        if (userChatRoomRepository.existsByIdUserIdAndIdChatroomId(currentUser.getId(), chatRoomId)) {
            throw new RuntimeException("User is already in this chat room");
        }

        UserChatRoom userRelation = UserChatRoom.builder()
                .id(UserChatRoom.UserChatRoomId.builder()
                        .userId(currentUser.getId())
                        .chatroomId(chatRoomId)
                        .build())
                .user(currentUser)
                .chatroom(chatRoom)
                .build();
        userChatRoomRepository.save(userRelation);
    }

    public void leaveChatRoom(Integer chatRoomId, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + chatRoomId));

        // Check if user is the leader
        if (chatRoom.getLeaderUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Chat room leader cannot leave the room. Transfer leadership or delete the room instead.");
        }

        // Check if user is in the chat room
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(currentUser.getId(), chatRoomId)) {
            throw new RuntimeException("User is not in this chat room");
        }

        userChatRoomRepository.deleteByUserIdAndChatroomId(currentUser.getId(), chatRoomId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatRoomResponseDTO> searchChatRooms(String searchBy, String search,
                                                              Integer page, Integer pageSize, String sortBy, String sortDirection) {

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortBy));

        Page<ChatRoom> chatRoomPage;

        switch (searchBy.toLowerCase()) {
            case "title":
                chatRoomPage = chatRoomRepository.findByTitleContaining(search, pageable);
                break;
            case "description":
                chatRoomPage = chatRoomRepository.findByDescriptionContaining(search, pageable);
                break;
            case "leader":
                chatRoomPage = chatRoomRepository.findByLeaderUsernameContaining(search, pageable);
                break;
            case "forceremain":
                Boolean forceRemain = Boolean.parseBoolean(search);
                chatRoomPage = chatRoomRepository.findByForceRemain(forceRemain, pageable);
                break;
            default:
                chatRoomPage = chatRoomRepository.findByAllFieldsContaining(search, pageable);
                break;
        }

        List<ChatRoomResponseDTO> chatRooms = chatRoomPage.getContent().stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<ChatRoomResponseDTO>builder()
                .data(chatRooms)
                .page(chatRoomPage.getNumber())
                .size(chatRoomPage.getSize())
                .totalElements(chatRoomPage.getTotalElements())
                .totalPages(chatRoomPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public int getUnreadMessagesCount(Integer userId) {
        // Get all chat rooms the user is a member of
        List<ChatRoom> userChatRooms = chatRoomRepository.findByUserId(userId);

        int totalUnreadCount = 0;

        // For each chat room, count unread messages not sent by the user
        for (ChatRoom chatRoom : userChatRooms) {
            // Count unread messages in this chat room that were not sent by the user
            long unreadInRoom = chatMessageRepository.countByChatroomIdAndIsReadFalseAndSenderIdNot(
                    chatRoom.getId(), userId);
            totalUnreadCount += unreadInRoom;
        }

        return totalUnreadCount;
    }
}
