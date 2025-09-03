package org.scit4bits.tonarinetserver.service;

import java.util.ArrayList;
import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TeamRequestDTO;
import org.scit4bits.tonarinetserver.dto.TeamResponseDTO;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.Team;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserTeam;
import org.scit4bits.tonarinetserver.repository.TeamRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.repository.UserTeamRepository;
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
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository;

    public TeamResponseDTO createTeam(TeamRequestDTO request, User creator) {
        log.info("Creating team with name: {} for organization: {} by user: {}", 
                request.getName(), request.getOrgId(), creator.getId());
        
        Team team = Team.builder()
                .name(request.getName())
                .leaderUserId(request.getMembers().get(0).getId()) // 첫번째 사람이 리더
                .orgId(request.getOrgId())
                .build();
        
        Team savedTeam = teamRepository.save(team);

        List<User> memberList = new ArrayList<>();

        for(UserDTO member : request.getMembers()) {
            User dbUser = userRepository.findById(member.getId()).get();
            memberList.add(dbUser);
        }
        savedTeam.setUsers(memberList);

        teamRepository.save(savedTeam);
        log.info("Team created successfully with id: {}", savedTeam.getId());
        return TeamResponseDTO.fromEntity(savedTeam);
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeams() {
        log.info("Fetching all teams");
        return teamRepository.findAll().stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(Integer id) {
        log.info("Fetching team with id: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        return TeamResponseDTO.fromEntity(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsByOrgId(Integer orgId) {
        log.info("Fetching teams for organization: {}", orgId);
        return teamRepository.findByOrgIdOrderByName(orgId).stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
    }

    public TeamResponseDTO updateTeam(Integer id, TeamRequestDTO request, User user) {
        log.info("Updating team with id: {} by user: {}", id, user.getId());
        
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        
        // Check if user is the leader or admin
        if (!team.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the team leader or admin can update the team");
        }
        
        team.setName(request.getName() != null && !request.getName().trim().isEmpty() 
                    ? request.getName() : team.getName());
        team.setOrgId(request.getOrgId() != null ? request.getOrgId() : team.getOrgId());
        
        Team savedTeam = teamRepository.save(team);
        log.info("Team updated successfully");
        return TeamResponseDTO.fromEntity(savedTeam);
    }

    public void deleteTeam(Integer id, User user) {
        log.info("Deleting team with id: {} by user: {}", id, user.getId());
        
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));
        
        // Check if user is the leader or admin
        if (!team.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("Only the team leader or admin can delete the team");
        }
        
        teamRepository.deleteById(id);
        log.info("Team deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedResponse<TeamResponseDTO> searchTeams(String searchBy, String search, Integer page, 
                                                     Integer pageSize, String sortBy, String sortDirection) {
        log.info("Searching teams with searchBy: {}, search: {}, page: {}, pageSize: {}, sortBy: {}, sortDirection: {}", 
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
            case "org":
                entityFieldName = "orgId";
                break;
            default:
                entityFieldName = "id";
                break;
        }
        
        Sort sort = Sort.by(sortDir, entityFieldName);
        Pageable pageable = PageRequest.of(pageNum, pageSizeNum, sort);
        
        Page<Team> teamPage;
        
        if (search == null || search.trim().isEmpty()) {
            teamPage = teamRepository.findAll(pageable);
        } else {
            switch (searchBy.toLowerCase()) {
                case "all":
                    teamPage = teamRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
                case "id":
                    try {
                        Integer searchId = Integer.parseInt(search.trim());
                        teamPage = teamRepository.findById(searchId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid ID format for search: {}", search);
                        teamPage = Page.empty(pageable);
                    }
                    break;
                case "name":
                    teamPage = teamRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
                    break;
                case "leader":
                    try {
                        Integer leaderId = Integer.parseInt(search.trim());
                        teamPage = teamRepository.findByLeaderUserId(leaderId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid leader ID format for search: {}", search);
                        teamPage = Page.empty(pageable);
                    }
                    break;
                case "org":
                    try {
                        Integer orgId = Integer.parseInt(search.trim());
                        teamPage = teamRepository.findByOrgId(orgId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid organization ID format for search: {}", search);
                        teamPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("Unknown searchBy parameter: {}. Using 'all' as default.", searchBy);
                    teamPage = teamRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }
        
        List<TeamResponseDTO> result = teamPage.getContent().stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
        
        log.info("Found {} teams out of {} total", result.size(), teamPage.getTotalElements());
        return new PagedResponse<>(result, pageNum, pageSizeNum, teamPage.getTotalElements(), teamPage.getTotalPages());
    }

    public void joinTeam(Integer teamId, User user) {
        log.info("User {} joining team {}", user.getId(), teamId);
        
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        // Check if user is already in the team
        // This would need to be implemented with the UserTeam junction table
        
        log.info("User {} joined team {} successfully", user.getId(), teamId);
    }

    public void leaveTeam(Integer teamId, User user) {
        log.info("User {} leaving team {}", user.getId(), teamId);
        
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        
        // Check if user is in the team and handle leaving
        // Leaders cannot leave unless they transfer leadership first
        
        log.info("User {} left team {} successfully", user.getId(), teamId);
    }
}
