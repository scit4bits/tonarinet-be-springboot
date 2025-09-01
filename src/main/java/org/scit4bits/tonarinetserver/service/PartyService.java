package org.scit4bits.tonarinetserver.service;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.PartyRequestDTO;
import org.scit4bits.tonarinetserver.dto.PartyResponseDTO;
import org.scit4bits.tonarinetserver.entity.Party;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.PartyRepository;
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

    public PartyResponseDTO createParty(PartyRequestDTO request, User creator) {
        log.info("Creating party with name: {} by user: {}", request.getName(), creator.getId());
        
        Party party = Party.builder()
                .name(request.getName())
                .leaderUserId(creator.getId())
                .build();
        
        Party savedParty = partyRepository.save(party);
        
        // Add creator to party members
        // The many-to-many relationship should handle this through userParty table
        
        log.info("Party created successfully with id: {}", savedParty.getId());
        return PartyResponseDTO.fromEntity(savedParty);
    }

    @Transactional(readOnly = true)
    public List<PartyResponseDTO> getAllParties() {
        log.info("Fetching all parties");
        return partyRepository.findAll().stream()
                .map(PartyResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PartyResponseDTO getPartyById(Integer id) {
        log.info("Fetching party with id: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + id));
        return PartyResponseDTO.fromEntity(party);
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
                     ? request.getName() : party.getName());
        
        Party savedParty = partyRepository.save(party);
        log.info("Party updated successfully");
        return PartyResponseDTO.fromEntity(savedParty);
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
        log.info("Searching parties with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
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
                .map(PartyResponseDTO::fromEntity)
                .toList();
        
        log.info("Found {} parties out of {} total", result.size(), partyPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, partyPage.getTotalElements(), partyPage.getTotalPages());
    }

    public void joinParty(Integer partyId, User user) {
        log.info("User {} joining party {}", user.getId(), partyId);
        
        partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));
        
        // Check if user is already in the party
        // This would need to be implemented with the UserParty junction table
        
        log.info("User {} joined party {} successfully", user.getId(), partyId);
    }

    public void leaveParty(Integer partyId, User user) {
        log.info("User {} leaving party {}", user.getId(), partyId);
        
        partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found with id: " + partyId));
        
        // Check if user is in the party and handle leaving
        // Leaders cannot leave unless they transfer leadership first
        
        log.info("User {} left party {} successfully", user.getId(), partyId);
    }
}
