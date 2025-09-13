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

/**
 * 채팅방 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 새로운 채팅방을 생성합니다.
     * @param requestDTO 채팅방 생성 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보 (방장)
     * @return 생성된 채팅방 정보
     */
    public ChatRoomResponseDTO createChatRoom(ChatRoomRequestDTO requestDTO, User currentUser) {
        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .forceRemain(requestDTO.getForceRemain() != null ? requestDTO.getForceRemain() : false)
                .leaderUserId(currentUser.getId())
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);

        // 방장을 채팅방에 추가
        UserChatRoom creatorRelation = UserChatRoom.builder()
                .id(UserChatRoom.UserChatRoomId.builder()
                        .userId(currentUser.getId())
                        .chatroomId(chatRoom.getId())
                        .build())
                .user(currentUser)
                .chatroom(chatRoom)
                .build();
        userChatRoomRepository.save(creatorRelation);

        // 다른 사용자를 채팅방에 추가
        if (requestDTO.getUserIds() != null && !requestDTO.getUserIds().isEmpty()) {
            for (Integer userId : requestDTO.getUserIds()) {
                if (!userId.equals(currentUser.getId())) { // 방장을 다시 추가하지 않도록 함
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

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

        // 관계가 설정된 완전한 채팅방 정보를 다시 조회
        ChatRoom savedChatRoom = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow(() -> new RuntimeException("생성 후 채팅방을 찾을 수 없습니다."));

        return ChatRoomResponseDTO.fromEntity(savedChatRoom);
    }

    /**
     * 해당 채팅방이 AI 채팅방인지 확인합니다.
     * @param roomId 채팅방 ID
     * @return AI 채팅방 여부
     */
    public boolean checkIfAIChatroom(Integer roomId) {
        ChatRoom chatroom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + roomId));
        // 방장 ID가 0이면 시스템(AI) 채팅방으로 간주
        return chatroom.getLeaderUserId().equals(0);
    }

    /**
     * 모든 채팅방 목록을 조회합니다.
     * @return ChatRoomResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 채팅방 정보를 조회합니다.
     * @param id 조회할 채팅방 ID
     * @return ChatRoomResponseDTO
     */
    @Transactional(readOnly = true)
    public ChatRoomResponseDTO getChatRoomById(Integer id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + id));
        return ChatRoomResponseDTO.fromEntity(chatRoom);
    }

    /**
     * 특정 사용자가 속한 모든 채팅방 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return ChatRoomResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getChatRoomsByUserId(Integer userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserId(userId);
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자가 방장인 모든 채팅방 목록을 조회합니다.
     * @param leaderId 방장 ID
     * @return ChatRoomResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getChatRoomsByLeaderId(Integer leaderId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByLeaderUserId(leaderId);
        return chatRooms.stream()
                .map(ChatRoomResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 정보를 수정합니다.
     * @param id 수정할 채팅방 ID
     * @param requestDTO 채팅방 수정 요청 정보
     * @param currentUser 현재 로그인한 사용자 정보
     * @return 수정된 채팅방 정보
     */
    public ChatRoomResponseDTO updateChatRoom(Integer id, ChatRoomRequestDTO requestDTO, User currentUser) {
        ChatRoom existingChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + id));

        // 사용자가 방장이거나 관리자인지 확인
        if (!existingChatRoom.getLeaderUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("방장 또는 관리자만 이 채팅방을 수정할 수 있습니다.");
        }

        // 채팅방 정보 수정
        existingChatRoom.setTitle(requestDTO.getTitle());
        existingChatRoom.setDescription(requestDTO.getDescription());
        if (requestDTO.getForceRemain() != null) {
            existingChatRoom.setForceRemain(requestDTO.getForceRemain());
        }

        ChatRoom updatedChatRoom = chatRoomRepository.save(existingChatRoom);

        // 사용자 멤버십 수정
        if (requestDTO.getUserIds() != null) {
            // 기존 사용자 관계 삭제 (방장 제외)
            userChatRoomRepository.deleteByChatroomIdAndUserIdNot(id, existingChatRoom.getLeaderUserId());

            // 새로운 사용자 관계 추가
            for (Integer userId : requestDTO.getUserIds()) {
                if (!userId.equals(existingChatRoom.getLeaderUserId())) { // 방장을 다시 추가하지 않도록 함
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

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

        // 수정된 완전한 채팅방 정보를 다시 조회
        ChatRoom finalChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("수정 후 채팅방을 찾을 수 없습니다."));

        return ChatRoomResponseDTO.fromEntity(finalChatRoom);
    }

    /**
     * 채팅방을 삭제합니다.
     * @param id 삭제할 채팅방 ID
     * @param currentUser 현재 로그인한 사용자 정보
     */
    public void deleteChatRoom(Integer id, User currentUser) {
        ChatRoom existingChatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + id));

        // 사용자가 방장이거나 관리자인지 확인
        if (!existingChatRoom.getLeaderUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new RuntimeException("방장 또는 관리자만 이 채팅방을 삭제할 수 있습니다.");
        }

        // 관련된 UserChatRoom 항목 먼저 삭제
        userChatRoomRepository.deleteByChatroomId(id);

        // 채팅방 삭제 (메시지는 cascade 삭제됨)
        chatRoomRepository.deleteById(id);
    }

    /**
     * 채팅방에 참여합니다.
     * @param chatRoomId 참여할 채팅방 ID
     * @param currentUser 현재 로그인한 사용자 정보
     */
    public void joinChatRoom(Integer chatRoomId, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

        // 사용자가 이미 채팅방에 있는지 확인
        if (userChatRoomRepository.existsByIdUserIdAndIdChatroomId(currentUser.getId(), chatRoomId)) {
            throw new RuntimeException("사용자가 이미 이 채팅방에 있습니다.");
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

    /**
     * 채팅방에서 나갑니다.
     * @param chatRoomId 나갈 채팅방 ID
     * @param currentUser 현재 로그인한 사용자 정보
     */
    public void leaveChatRoom(Integer chatRoomId, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

        // 사용자가 방장인지 확인
        if (chatRoom.getLeaderUserId().equals(currentUser.getId())) {
            throw new RuntimeException("방장은 채팅방을 나갈 수 없습니다. 방장을 위임하거나 채팅방을 삭제하세요.");
        }

        // 사용자가 채팅방에 있는지 확인
        if (!userChatRoomRepository.existsByIdUserIdAndIdChatroomId(currentUser.getId(), chatRoomId)) {
            throw new RuntimeException("사용자가 이 채팅방에 없습니다.");
        }

        userChatRoomRepository.deleteByUserIdAndChatroomId(currentUser.getId(), chatRoomId);
    }

    /**
     * 채팅방을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 ChatRoomResponseDTO
     */
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

    /**
     * 읽지 않은 메시지 총 개수를 조회합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 메시지 개수
     */
    @Transactional(readOnly = true)
    public int getUnreadMessagesCount(Integer userId) {
        // 사용자가 멤버인 모든 채팅방을 가져옵니다.
        List<ChatRoom> userChatRooms = chatRoomRepository.findByUserId(userId);

        int totalUnreadCount = 0;

        // 각 채팅방에 대해 사용자가 보내지 않은 읽지 않은 메시지를 계산합니다.
        for (ChatRoom chatRoom : userChatRooms) {
            long unreadInRoom = chatMessageRepository.countByChatroomIdAndIsReadFalseAndSenderIdNot(
                    chatRoom.getId(), userId);
            totalUnreadCount += unreadInRoom;
        }

        return totalUnreadCount;
    }
}
