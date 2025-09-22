package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.*;
import org.scit4bits.tonarinetserver.entity.Party;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserParty;
import org.scit4bits.tonarinetserver.repository.PartyRepository;
import org.scit4bits.tonarinetserver.repository.UserPartyRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 파티(모임) 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PartyService {

    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final UserPartyRepository userPartyRepository;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;

    /**
     * 새로운 파티를 생성하고, 연관된 채팅방을 함께 생성합니다.
     * @param request 파티 생성 요청 정보
     * @param creator 파티 생성자 정보
     * @return 생성된 파티 정보
     */
    public PartyResponseDTO createParty(PartyRequestDTO request, User creator) {
        log.info("파티 생성 - 이름: {}, 생성자: {}", request.getName(), creator.getId());

        Party party = Party.builder()
                .name(request.getName())
                .leaderUserId(creator.getId())
                .isFinished(false)
                .build();

        Party savedParty = partyRepository.save(party);

        // 파티 생성자는 자동으로 멤버로 추가 및 승인됩니다.
        UserParty newUserParty = UserParty.builder()
                .id(UserParty.UserPartyId.builder()
                        .userId(creator.getId())
                        .partyId(savedParty.getId())
                        .build())
                .user(creator)
                .party(savedParty)
                .isGranted(true)
                .build();
        userPartyRepository.save(newUserParty);

        // 파티와 연동될 새로운 채팅방 생성
        ChatRoomRequestDTO chatRoomRequest = ChatRoomRequestDTO.builder()
                .title(savedParty.getName()) // 파티 이름을 채팅방 제목으로 사용
                .description("파티를 위한 채팅방: " + savedParty.getName())
                .forceRemain(false)
                .build();

        ChatRoomResponseDTO chatRoomResponse = chatRoomService.createChatRoom(chatRoomRequest, creator);
        log.info("파티 {}를 위한 채팅방 {}가 성공적으로 생성되었습니다.", savedParty.getId(), chatRoomResponse.getId());

        log.info("파티가 성공적으로 생성되었습니다. ID: {}", savedParty.getId());
        return createPartyResponseDTOWithUserPartyData(savedParty);
    }

    /**
     * 모든 파티 목록을 조회합니다.
     * @return PartyResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<PartyResponseDTO> getAllParties() {
        log.info("모든 파티 조회");
        return partyRepository.findAll().stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();
    }

    /**
     * ID로 특정 파티 정보를 조회합니다.
     * @param id 조회할 파티 ID
     * @return PartyResponseDTO
     */
    @Transactional(readOnly = true)
    public PartyResponseDTO getPartyById(Integer id) {
        log.info("ID로 파티 조회: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + id));
        return createPartyResponseDTOWithUserPartyData(party);
    }

    /**
     * UserParty 데이터를 UserDTO에 주입하여 PartyResponseDTO를 생성합니다.
     * @param party 파티 엔티티
     * @return 사용자 정보가 보강된 PartyResponseDTO
     */
    private PartyResponseDTO createPartyResponseDTOWithUserPartyData(Party party) {
        List<UserDTO> enrichedUsers = null;
        if (party.getUsers() != null) {
            enrichedUsers = party.getUsers().stream()
                    .map(user -> {
                        UserDTO userDTO = UserDTO.fromEntity(user);

                        // UserParty 정보를 조회하여 가입 메시지와 승인 상태를 주입합니다.
                        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                                .userId(user.getId())
                                .partyId(party.getId())
                                .build();

                        userPartyRepository.findById(userPartyId).ifPresent(userParty -> {
                            log.debug("파티 {}의 사용자 {}에게 UserParty 데이터 주입: entryMessage='{}', isGranted={}",
                                    party.getId(), user.getId(), userParty.getEntryMessage(), userParty.getIsGranted());
                            userDTO.setEntryMessage(userParty.getEntryMessage());
                            userDTO.setIsGranted(userParty.getIsGranted());
                        });

                        return userDTO;
                    })
                    .toList();
        }

        return PartyResponseDTO.builder()
                .id(party.getId())
                .name(party.getName())
                .leaderUserId(party.getLeaderUserId())
                .leaderUserName(party.getLeaderUser() != null ? party.getLeaderUser().getName() : null)
                .users(enrichedUsers)
                .userCount(enrichedUsers != null ? enrichedUsers.size() : 0)
                .isFinished(party.getIsFinished())
                .build();
    }

    /**
     * 파티 정보를 수정합니다.
     * @param id 수정할 파티 ID
     * @param request 파티 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 파티 정보
     */
    public PartyResponseDTO updateParty(Integer id, PartyRequestDTO request, User user) {
        log.info("파티 정보 수정 - ID: {}, 사용자: {}", id, user.getId());

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + id));

        // 사용자가 파티장이거나 관리자인지 확인
        if (!party.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("파티장 또는 관리자만 파티 정보를 수정할 수 있습니다.");
        }

        party.setName(request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName()
                : party.getName());

        Party savedParty = partyRepository.save(party);
        log.info("파티 정보 수정 완료");
        return createPartyResponseDTOWithUserPartyData(savedParty);
    }

    /**
     * 파티를 삭제합니다.
     * @param id 삭제할 파티 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteParty(Integer id, User user) {
        log.info("파티 삭제 - ID: {}, 사용자: {}", id, user.getId());

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + id));

        // 사용자가 파티장이거나 관리자인지 확인
        if (!party.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("파티장 또는 관리자만 파티를 삭제할 수 있습니다.");
        }

        partyRepository.deleteById(id);
        log.info("파티 삭제 완료");
    }

    /**
     * 파티를 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 PartyResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<PartyResponseDTO> searchParties(String searchBy, String search, Integer page,
                                                         Integer pageSize, String sortBy, String sortDirection) {
        log.info(
                "파티 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        String entityFieldName = switch (sortByField.toLowerCase()) {
            case "id" -> "id";
            case "name" -> "name";
            case "leader" -> "leaderUserId";
            default -> "id";
        };

        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);

        Page<Party> partyPage;

        if (search == null || search.trim().isEmpty()) {
            partyPage = partyRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    partyPage = partyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        partyPage = partyRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
                        partyPage = Page.empty(pageable);
                    }
                    break;
                case "name":
                    partyPage = partyRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "leader":
                    try {
                        Integer leaderId = Integer.parseInt(search.trim());
                        partyPage = partyRepository.findByLeaderUserId(leaderId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 파티장 ID 형식으로 검색: {}", search);
                        partyPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    partyPage = partyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<PartyResponseDTO> result = partyPage.getContent().stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();

        log.info("총 {}개의 파티 중 {}개를 찾았습니다.", partyPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, partyPage.getTotalElements(),
                partyPage.getTotalPages());
    }

    /**
     * 파티에 가입을 신청합니다.
     * @param partyId 가입할 파티 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void joinParty(Integer partyId, User user, PartyJoinRequestDTO request) {
        log.info("사용자 {}가 파티 {}에 가입을 요청합니다.", user.getId(), partyId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + partyId));

        // 이미 파티에 속해 있는지 확인
        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(user.getId())
                .partyId(partyId)
                .build();
        if (userPartyRepository.existsById(userPartyId)) {
            throw new RuntimeException("사용자가 이미 이 파티에 속해 있습니다.");
        }

        // isGranted: false로 UserParty 생성 (승인 대기)
        UserParty userParty = UserParty.builder()
                .id(userPartyId)
                .user(user)
                .party(party)
                .isGranted(false) // 채팅방 입장을 위해 승인이 필요합니다.
                .entryMessage(request.getEntryMessage())
                .build();
        userPartyRepository.save(userParty);

        // 파티장에게 가입 요청 알림 생성
        notificationService.addNotification(party.getLeaderUserId(),
                "{\"messageType\": \"incomingPartyRequest\", \"partyName\": \"" + party.getName() + "\", \"userName\": \"" + user.getName() + "\"}",
                null);

        log.info("사용자 {}가 파티 {}에 성공적으로 가입 요청했습니다. (승인 대기 중)", user.getId(), partyId);
    }

    /**
     * 파티 가입 신청을 승인합니다.
     * @param partyId 파티 ID
     * @param targetUserId 대상 사용자 ID
     * @param grantor 승인자 정보
     */
    public void grantUserForParty(Integer partyId, Integer targetUserId, User grantor) {
        log.info("사용자 {}가 파티 {}에 대한 사용자 {}의 접근을 승인합니다.", grantor.getId(), partyId, targetUserId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + partyId));

        // 승인자가 파티장이거나 관리자인지 확인
        if (!party.getLeaderUserId().equals(grantor.getId()) && !grantor.getIsAdmin()) {
            throw new RuntimeException("파티장 또는 관리자만 접근을 승인할 수 있습니다.");
        }

        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(targetUserId)
                .partyId(partyId)
                .build();

        UserParty userParty = userPartyRepository.findById(userPartyId)
                .orElseThrow(() -> new RuntimeException("사용자가 이 파티에 가입을 요청하지 않았습니다."));

        if (Boolean.TRUE.equals(userParty.getIsGranted())) {
            throw new RuntimeException("사용자는 이미 이 파티에 대한 접근이 승인되었습니다.");
        }

        userParty.setIsGranted(true);
        userPartyRepository.save(userParty);

        // 해당 파티의 채팅방에 사용자를 참여시킵니다.
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다."));

        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.searchChatRooms("title", party.getName(), 0, 10, "id", "asc").getData();

            ChatRoomResponseDTO matchingChatRoom = chatRooms.stream()
                    .filter(cr -> cr.getTitle().equals(party.getName()))
                    .findFirst()
                    .orElse(null);

            if (matchingChatRoom != null) {
                chatRoomService.joinChatRoom(matchingChatRoom.getId(), targetUser);
                log.info("사용자 {}가 파티 {}의 채팅방 {}에 참여했습니다.",
                        targetUserId, partyId, matchingChatRoom.getId());
            } else {
                log.warn("파티 {}에 해당하는 채팅방을 찾을 수 없습니다. 이름: '{}'", partyId, party.getName());
            }
        } catch (Exception e) {
            log.warn("사용자 {}를 파티 {}의 채팅방에 참여시키는 데 실패했습니다: {}", targetUserId, partyId, e.getMessage());
        }

        // 대상 사용자에게 접근 승인 알림 생성
        notificationService.addNotification(targetUserId,
                "{\"messageType\": \"approvedPartyRequest\", \"partyName\": \"" + party.getName() + "\"}",
                null);

        log.info("사용자 {}의 파티 {} 접근을 성공적으로 승인했습니다.", targetUserId, partyId);
    }

    /**
     * 파티 가입 신청을 거절합니다.
     * @param partyId 파티 ID
     * @param targetUserId 대상 사용자 ID
     * @param rejector 거절자 정보
     */
    public void rejectUserForParty(Integer partyId, Integer targetUserId, User rejector) {
        log.info("사용자 {}가 파티 {}에 대한 사용자 {}의 접근을 거절합니다.", rejector.getId(), partyId, targetUserId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + partyId));

        if (!party.getLeaderUserId().equals(rejector.getId()) && !rejector.getIsAdmin()) {
            throw new RuntimeException("파티장 또는 관리자만 접근을 거절할 수 있습니다.");
        }

        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(targetUserId)
                .partyId(partyId)
                .build();

        UserParty userParty = userPartyRepository.findById(userPartyId)
                .orElseThrow(() -> new RuntimeException("사용자가 이 파티에 가입을 요청하지 않았습니다."));

        if (Boolean.TRUE.equals(userParty.getIsGranted())) {
            throw new RuntimeException("이미 승인된 사용자입니다. 탈퇴/추방 기능을 사용하세요.");
        }

        userPartyRepository.delete(userParty);

        notificationService.addNotification(targetUserId,
                "{\"messageType\": \"rejectedPartyRequest\", \"partyName\": \"" + party.getName() + "\"}",
                null);

        log.info("사용자 {}를 파티 {}에서 성공적으로 거절했습니다.", targetUserId, partyId);
    }

    /**
     * 파티에서 나갑니다.
     * @param partyId 나갈 파티 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void leaveParty(Integer partyId, User user) {
        log.info("사용자 {}가 파티 {}에서 나갑니다.", user.getId(), partyId);

        partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다. ID: " + partyId));

        // TODO: 사용자가 파티에 있는지 확인하고 나가는 로직 구현
        // 파티장은 리더십을 위임하기 전에는 나갈 수 없습니다.

        log.info("사용자 {}가 파티 {}에서 성공적으로 나갔습니다.", user.getId(), partyId);
    }

    /**
     * 특정 사용자가 속한 파티 목록을 조회합니다.
     * @param user 사용자 정보
     * @return PartyResponseDTO 리스트
     */
    public List<PartyResponseDTO> getPartiesByUserId(User user) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + user.getId()));

        List<Party> parties = dbUser.getJoinedParties();

        return parties.stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();
    }
}
