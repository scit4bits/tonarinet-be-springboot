package org.scit4bits.tonarinetserver.service;

import java.util.List;
import java.util.stream.Collectors;

import org.scit4bits.tonarinetserver.config.WebSocketConfig.UserPrincipal;
import org.scit4bits.tonarinetserver.dto.ChatMessageRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatMessageResponseDTO;
import org.scit4bits.tonarinetserver.entity.ChatMessage;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ChatMessageRepository;
import org.scit4bits.tonarinetserver.repository.ChatRoomRepository;
import org.scit4bits.tonarinetserver.repository.UserChatRoomRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    /**
     * Send a message to a chat room
     */
    public ChatMessageResponseDTO sendMessage(ChatMessageRequestDTO requestDTO, Integer senderId) {
        // Verify that the chat room exists
        chatRoomRepository.findById(requestDTO.getChatroomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found with ID: " + requestDTO.getChatroomId()));

        // Verify that the sender is a member of the chat room
        // if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(sender.getId(),
        // requestDTO.getChatroomId())) {
        // throw new RuntimeException("User is not a member of this chat room");
        // }

        // Create and save the chat message
        ChatMessage chatMessage = ChatMessage.builder()
                .chatroomId(requestDTO.getChatroomId())
                .senderId(senderId)
                .message(requestDTO.getMessage())
                .isRead(false)
                .build();

        chatMessage = chatMessageRepository.save(chatMessage);

        // Fetch the saved message with sender information
        ChatMessage savedMessage = chatMessageRepository.findById(chatMessage.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve saved message"));

        return ChatMessageResponseDTO.fromEntity(savedMessage);
    }

    /**
     * Get messages for a chat room with pagination
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessagesByChatRoom(Integer chatroomId, Integer page, Integer size,
            User user) {
        // Verify that the user is a member of the chat room
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("User is not a member of this chat room");
        }

        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 50);
        List<ChatMessage> messages = chatMessageRepository.findRecentMessagesByChatroomId(chatroomId, pageable);

        return messages.stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all messages for a chat room (ordered by creation time)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getAllMessagesByChatRoom(Integer chatroomId, User user) {
        // Verify that the user is a member of the chat room
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("User is not a member of this chat room");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatroomIdOrderByCreatedAtAsc(chatroomId);

        return messages.stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Mark messages as read (this could be extended for per-user read status)
     */
    public void markMessagesAsRead(Integer chatroomId, User user) {
        // Verify that the user is a member of the chat room
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("User is not a member of this chat room");
        }

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatroomIdAndIsReadFalse(chatroomId);
        unreadMessages.forEach(message -> message.setIsRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * Get message count for a chat room
     */
    @Transactional(readOnly = true)
    public Long getMessageCount(Integer chatroomId) {
        return chatMessageRepository.countByChatroomId(chatroomId);
    }

    /**
     * Delete a message (only by sender or admin)
     */
    public void deleteMessage(Integer messageId, User user) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));

        // Check if user is the sender or admin
        if (!message.getSenderId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the sender or admin can delete this message");
        }

        chatMessageRepository.delete(message);
    }
}
