package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.ChatRoomRequestDTO;
import org.scit4bits.tonarinetserver.dto.ChatRoomResponseDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.PartyRequestDTO;
import org.scit4bits.tonarinetserver.dto.PartyResponseDTO;
import org.scit4bits.tonarinetserver.dto.UserDTO;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public PartyResponseDTO createParty(PartyRequestDTO request, User creator) {
        log.info("Creating party with name: {} by user: {}", request.getName(), creator.getId());

        Party party = Party.builder()
                .name(request.getName())
                .leaderUserId(creator.getId())
                .build();

        Party savedParty = partyRepository.save(party);


        UserParty newUserParty = UserParty.builder()
        .id(UserParty.UserPartyId.builder()
                .userId(creator.getId())
                .partyId(savedParty.getId())
                .build())
        .user(creator)
        .party(savedParty)
        .isGranted(true) // Party creator is automatically granted
        .build();
        // Add creator to party members
        // The many-to-many relationship should handle this through userParty table
        userPartyRepository.save(newUserParty);

        // create new ChatRoom and join the creator
        ChatRoomRequestDTO chatRoomRequest = ChatRoomRequestDTO.builder()
                .title(savedParty.getName()) // Use party name as chat room title
                .description("Chat room for party: " + savedParty.getName())
                .forceRemain(false)
                .build();
        
        ChatRoomResponseDTO chatRoomResponse = chatRoomService.createChatRoom(chatRoomRequest, creator);
        log.info("ChatRoom created successfully with id: {} for party: {}", 
                 chatRoomResponse.getId(), savedParty.getId());

        log.info("Party created successfully with id: {}", savedParty.getId());
        return createPartyResponseDTOWithUserPartyData(savedParty);
    }

    @Transactional(readOnly = true)
    public List<PartyResponseDTO> getAllParties() {
        log.info("Fetching all parties");
        return partyRepository.findAll().stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartyResponseDTO getPartyById(Integer id) {
        log.info("Fetching party with id: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + id));
        return createPartyResponseDTOWithUserPartyData(party);
    }

    /**
     * Create PartyResponseDTO with UserParty data (entryMessage, isGranted) injected into UserDTO objects
     */
    private PartyResponseDTO createPartyResponseDTOWithUserPartyData(Party party) {
        List<UserDTO> enrichedUsers = null;
        if (party.getUsers() != null) {
            enrichedUsers = party.getUsers().stream()
                    .map(user -> {
                        UserDTO userDTO = UserDTO.fromEntity(user);
                        
                        // Fetch UserParty data to get entryMessage and isGranted
                        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                                .userId(user.getId())
                                .partyId(party.getId())
                                .build();
                        
                        userPartyRepository.findById(userPartyId).ifPresent(userParty -> {
                            log.debug("Injecting UserParty data for user {} in party {}: entryMessage='{}', isGranted={}",
                                    user.getId(), party.getId(), userParty.getEntryMessage(), userParty.getIsGranted());
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
                .build();
    }

    public PartyResponseDTO updateParty(Integer id, PartyRequestDTO request, User user) {
        log.info("Updating party with id: {} by user: {}", id, user.getId());

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + id));

        // Check if user is the leader
        if (!party.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the party leader or admin can update the party");
        }

        party.setName(request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName()
                : party.getName());

        Party savedParty = partyRepository.save(party);
        log.info("Party updated successfully");
        return createPartyResponseDTOWithUserPartyData(savedParty);
    }

    public void deleteParty(Integer id, User user) {
        log.info("Deleting party with id: {} by user: {}", id, user.getId());

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + id));

        // Check if user is the leader or admin
        if (!party.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the party leader or admin can delete the party");
        }

        partyRepository.deleteById(id);
        log.info("Party deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedResponse<PartyResponseDTO> searchParties(String searchBy, String search, Integer page,
            Integer pageSize, String sortBy, String sortDirection) {
        log.info(
                "Searching parties with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
                searchBy, search, page, pageSize, sortBy, sortDirection);

        // 기본값 설정
        int pageNum = (page != null) ? page : 0;
        int pageSizeNum = (pageSize != null) ? pageSize : 10;
        String sortByField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "id";
        String direction = (sortDirection != null && !sortDirection.isEmpty()) ? sortDirection : "asc";

        // 정렬 방향 설정
        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // sortBy 필드명 매핑
        String entityFieldName;
        switch (sortByField.toLowerCase()) {
            case "id":
                entityFieldName = "id";
                break;
            case "name":
                entityFieldName = "name";
                break;
            case "leader":
                entityFieldName = "leaderUserId";
                break;
            default:
                entityFieldName = "id";
                break;
        }

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
                        log.warn("Invalid ID format for search: {}", search);
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
                        log.warn("Invalid leader ID format for search: {}", search);
                        partyPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    partyPage = partyRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<PartyResponseDTO> result = partyPage.getContent().stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();

        log.info("Found {} parties out of {} total", result.size(), partyPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, partyPage.getTotalElements(),
                partyPage.getTotalPages());
    }

    public void joinParty(Integer partyId, User user) {
        log.info("User {} requesting to join party {}", user.getId(), partyId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Check if user is already in the party
        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(user.getId())
                .partyId(partyId)
                .build();
        boolean isAlreadyInParty = userPartyRepository.existsById(userPartyId);
        if (isAlreadyInParty) {
            throw new RuntimeException("User is already in this party");
        }

        // Create UserParty with isGranted: false (pending approval)
        UserParty userParty = UserParty.builder()
                .id(UserParty.UserPartyId.builder()
                        .userId(user.getId())
                        .partyId(partyId)
                        .build())
                .user(user)
                .party(party)
                .isGranted(false) // User needs approval to enter chatting room
                .build();
        userPartyRepository.save(userParty);

        // Create notification for party leader about join request
        notificationService.addNotification(party.getLeaderUserId(), 
                "User " + user.getName() + " requested to join party " + party.getName(), 
                "/party/" + partyId);

        log.info("User {} requested to join party {} successfully (pending approval)", user.getId(), partyId);
    }

    public void grantUserForParty(Integer partyId, Integer targetUserId, User grantor) {
        log.info("User {} granting access to user {} for party {}", grantor.getId(), targetUserId, partyId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Check if grantor is the party leader or admin
        if (!party.getLeaderUserId().equals(grantor.getId()) && !grantor.getIsAdmin()) {
            throw new RuntimeException("Only the party leader or admin can grant access to the party");
        }

        // Find the UserParty entry
        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(targetUserId)
                .partyId(partyId)
                .build();
        
        UserParty userParty = userPartyRepository.findById(userPartyId)
                .orElseThrow(() -> new RuntimeException("User is not requesting to join this party"));

        // Check if already granted
        if (Boolean.TRUE.equals(userParty.getIsGranted())) {
            throw new RuntimeException("User is already granted access to this party");
        }

        // Grant access
        userParty.setIsGranted(true);
        userPartyRepository.save(userParty);

        // Find corresponding ChatRoom by party name and join user to it
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        try {
            List<ChatRoomResponseDTO> chatRooms = chatRoomService.searchChatRooms("title", party.getName(), 0, 10, "id", "asc").getData();
            
            // Find exact match for party name
            ChatRoomResponseDTO matchingChatRoom = chatRooms.stream()
                    .filter(cr -> cr.getTitle().equals(party.getName()))
                    .findFirst()
                    .orElse(null);
            
            if (matchingChatRoom != null) {
                chatRoomService.joinChatRoom(matchingChatRoom.getId(), targetUser);
                log.info("User {} joined corresponding ChatRoom {} for party {}", 
                         targetUserId, matchingChatRoom.getId(), partyId);
            } else {
                log.warn("No matching ChatRoom found for party {} with name '{}'", partyId, party.getName());
            }
        } catch (Exception e) {
            log.warn("Failed to join user {} to ChatRoom for party {}: {}", targetUserId, partyId, e.getMessage());
            // Don't fail the grant if ChatRoom join fails
        }

        // Create notification for target user about being granted access
        notificationService.addNotification(targetUserId, 
                "Your request to join party " + party.getName() + " has been approved!", 
                "/party/" + partyId);

        log.info("User {} granted access to party {} successfully", targetUserId, partyId);
    }

    public void rejectUserForParty(Integer partyId, Integer targetUserId, User rejector) {
        log.info("User {} rejecting access to user {} for party {}", rejector.getId(), targetUserId, partyId);

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Check if rejector is the party leader or admin
        if (!party.getLeaderUserId().equals(rejector.getId()) && !rejector.getIsAdmin()) {
            throw new RuntimeException("Only the party leader or admin can reject access to the party");
        }

        // Find the UserParty entry
        UserParty.UserPartyId userPartyId = UserParty.UserPartyId.builder()
                .userId(targetUserId)
                .partyId(partyId)
                .build();
        
        UserParty userParty = userPartyRepository.findById(userPartyId)
                .orElseThrow(() -> new RuntimeException("User is not requesting to join this party"));

        // Check if already granted (can't reject already granted user through this method)
        if (Boolean.TRUE.equals(userParty.getIsGranted())) {
            throw new RuntimeException("User is already granted access. Use leave/kick functionality instead");
        }

        // Remove the UserParty entry (reject the request)
        userPartyRepository.delete(userParty);

        // Create notification for target user about being rejected
        notificationService.addNotification(targetUserId, 
                "Your request to join party " + party.getName() + " has been rejected.", 
                "/party/" + partyId);

        log.info("User {} rejected from party {} successfully", targetUserId, partyId);
    }

    public void leaveParty(Integer partyId, User user) {
        log.info("User {} leaving party {}", user.getId(), partyId);

        partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));

        // Check if user is in the party and handle leaving
        // Leaders cannot leave unless they transfer leadership first

        log.info("User {} left party {} successfully", user.getId(), partyId);
    }

    public List<PartyResponseDTO> getPartiesByUserId(User user) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));

        List<Party> parties = dbUser.getJoinedParties();

        return parties.stream()
                .map(this::createPartyResponseDTOWithUserPartyData)
                .toList();
    }
}
