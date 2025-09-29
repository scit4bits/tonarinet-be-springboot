package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ChatMessageRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatMessageResponseDTO;
import org.scit4bits.tonarinetserver.entity.ChatMessage;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.ChatMessageRepository;
import org.scit4bits.tonarinetserver.repository.ChatRoomRepository;
import org.scit4bits.tonarinetserver.repository.UserChatRoomRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 메시지 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    /**
     * 채팅방에 메시지를 전송합니다.
     * @param requestDTO 메시지 요청 정보
     * @param senderId 발신자 ID
     * @return 전송된 메시지 정보
     */
    public ChatMessageResponseDTO sendMessage(ChatMessageRequestDTO requestDTO, Integer senderId) {
        // 채팅방 존재 여부 확인
        chatRoomRepository.findById(requestDTO.getChatroomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + requestDTO.getChatroomId()));

        // 발신자가 채팅방의 멤버인지 확인 (주석 처리됨)
        // if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(sender.getId(),
        // requestDTO.getChatroomId())) {
        // throw new RuntimeException("사용자가 이 채팅방의 멤버가 아닙니다.");
        // }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("발신자 사용자를 찾을 수 없습니다. ID: " + senderId));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatroomId(requestDTO.getChatroomId())
                .senderId(senderId)
                .message(requestDTO.getMessage())
                .isRead(false)
                .build();
        chatMessageRepository.save(chatMessage);

        // 발신자 정보를 포함하여 저장된 메시지를 다시 조회합니다.
        ChatMessage savedMessage = chatMessageRepository.findById(chatMessage.getId())
                .orElseThrow(() -> new RuntimeException("저장된 메시지를 가져오는데 실패했습니다."));

        ChatMessageResponseDTO responseDTO = ChatMessageResponseDTO.fromEntity(savedMessage);
        responseDTO.setSenderNickname(sender.getNickname()); // 직접 주입
        responseDTO.setSenderProfileFileId(sender.getProfileFileId()); // 직접 주입

        return responseDTO;
    }

    /**
     * 특정 채팅방의 메시지 목록을 페이징하여 조회합니다.
     * @param chatroomId 채팅방 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param user 현재 로그인한 사용자 정보
     * @return 페이징 처리된 ChatMessageResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessagesByChatRoom(Integer chatroomId, Integer page, Integer size,
                                                              User user) {
        // 사용자가 채팅방의 멤버인지 확인
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("사용자가 이 채팅방의 멤버가 아닙니다.");
        }

        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 50);
        List<ChatMessage> messages = chatMessageRepository.findRecentMessagesByChatroomId(chatroomId, pageable);

        return messages.stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다. (생성 시간순)
     * @param chatroomId 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     * @return ChatMessageResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getAllMessagesByChatRoom(Integer chatroomId, User user) {
        // 사용자가 채팅방의 멤버인지 확인
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("사용자가 이 채팅방의 멤버가 아닙니다.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByChatroomIdOrderByCreatedAtAsc(chatroomId);

        return messages.stream()
                .map(ChatMessageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 메시지를 읽음으로 표시합니다.
     * @param chatroomId 채팅방 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void markMessagesAsRead(Integer chatroomId, User user) {
        // 사용자가 채팅방의 멤버인지 확인
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(user.getId(), chatroomId)) {
            throw new RuntimeException("사용자가 이 채팅방의 멤버가 아닙니다.");
        }

        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatroomIdAndIsReadFalse(chatroomId);
        unreadMessages.forEach(message -> message.setIsRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * 특정 채팅방의 메시지 수를 조회합니다.
     * @param chatroomId 채팅방 ID
     * @return 메시지 수
     */
    @Transactional(readOnly = true)
    public Long getMessageCount(Integer chatroomId) {
        return chatMessageRepository.countByChatroomId(chatroomId);
    }

    /**
     * 메시지를 삭제합니다. (발신자 또는 관리자만 가능)
     * @param messageId 삭제할 메시지 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteMessage(Integer messageId, User user) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다. ID: " + messageId));

        // 사용자가 발신자이거나 관리자인지 확인
        if (!message.getSenderId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("발신자 또는 관리자만 이 메시지를 삭제할 수 있습니다.");
        }

        chatMessageRepository.delete(message);
    }
}
