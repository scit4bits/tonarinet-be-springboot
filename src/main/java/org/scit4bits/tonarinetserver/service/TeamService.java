package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.TeamRequestDTO;
import org.scit4bits.tonarinetserver.dto.TeamResponseDTO;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.Team;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.TeamRepository;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.repository.UserTeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 팀 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 팀을 생성합니다.
     * @param request 팀 생성 요청 정보
     * @param creator 팀 생성자 정보
     * @return 생성된 팀 정보
     */
    public TeamResponseDTO createTeam(TeamRequestDTO request, User creator) {
        log.info("팀 생성 - 이름: {}, 조직: {}, 생성자: {}",
                request.getName(), request.getOrgId(), creator.getId());

        Team team = Team.builder()
                .name(request.getName())
                .leaderUserId(request.getMembers().get(0).getId()) // 요청 멤버 목록의 첫 번째 사용자를 리더로 설정
                .orgId(request.getOrgId())
                .build();

        Team savedTeam = teamRepository.save(team);

        List<User> memberList = new ArrayList<>();

        for (UserDTO member : request.getMembers()) {
            User dbUser = userRepository.findById(member.getId()).get();
            memberList.add(dbUser);
        }
        savedTeam.setUsers(memberList);

        teamRepository.save(savedTeam);
        log.info("팀 생성 완료, ID: {}", savedTeam.getId());
        return TeamResponseDTO.fromEntity(savedTeam);
    }

    /**
     * 모든 팀 목록을 조회합니다.
     * @return TeamResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getAllTeams() {
        log.info("모든 팀 조회");
        return teamRepository.findAll().stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
    }

    /**
     * ID로 특정 팀 정보를 조회합니다.
     * @param id 조회할 팀 ID
     * @return TeamResponseDTO
     */
    @Transactional(readOnly = true)
    public TeamResponseDTO getTeamById(Integer id) {
        log.info("ID로 팀 조회: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + id));
        return TeamResponseDTO.fromEntity(team);
    }

    /**
     * 특정 조직에 속한 모든 팀을 조회합니다.
     * @param orgId 조직 ID
     * @return TeamResponseDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TeamResponseDTO> getTeamsByOrgId(Integer orgId) {
        log.info("조직 {}의 팀 조회", orgId);
        return teamRepository.findByOrgIdOrderByName(orgId).stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 특정 사용자가 속한 모든 팀을 조회합니다.
     * @param user 사용자 정보
     * @return TeamResponseDTO 리스트
     */
    public List<TeamResponseDTO> getMyTeams(User user) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + user.getId()));

        List<Team> teams = dbUser.getTeams();
        return teams.stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();
    }

    /**
     * 팀 정보를 수정합니다.
     * @param id 수정할 팀 ID
     * @param request 팀 수정 요청 정보
     * @param user 현재 로그인한 사용자 정보
     * @return 수정된 팀 정보
     */
    public TeamResponseDTO updateTeam(Integer id, TeamRequestDTO request, User user) {
        log.info("팀 정보 수정 - ID: {}, 사용자: {}", id, user.getId());

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + id));

        // 사용자가 팀 리더이거나 관리자인지 확인
        if (!team.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("팀 리더 또는 관리자만 팀 정보를 수정할 수 있습니다.");
        }

        team.setName(request.getName() != null && !request.getName().trim().isEmpty()
                ? request.getName()
                : team.getName());
        team.setOrgId(request.getOrgId() != null ? request.getOrgId() : team.getOrgId());

        Team savedTeam = teamRepository.save(team);
        log.info("팀 정보 수정 완료");
        return TeamResponseDTO.fromEntity(savedTeam);
    }

    /**
     * 팀을 삭제합니다.
     * @param id 삭제할 팀 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void deleteTeam(Integer id, User user) {
        log.info("팀 삭제 - ID: {}, 사용자: {}", id, user.getId());

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + id));

        // 사용자가 팀 리더이거나 관리자인지 확인
        if (!team.getLeaderUserId().equals(user.getId()) && !user.getIsAdmin()) {
            throw new RuntimeException("팀 리더 또는 관리자만 팀을 삭제할 수 있습니다.");
        }

        teamRepository.deleteById(id);
        log.info("팀 삭제 완료");
    }

    /**
     * 팀을 검색합니다.
     * @param searchBy 검색 기준
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return 페이징 처리된 TeamResponseDTO
     */
    @Transactional(readOnly = true)
    public PagedResponse<TeamResponseDTO> searchTeams(String searchBy, String search, Integer page,
                                                      Integer pageSize, String sortBy, String sortDirection) {
        log.info("팀 검색 - 기준: {}, 검색어: {}, 페이지: {}, 크기: {}, 정렬: {}:{}",
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
            case "org" -> "orgId";
            default -> "id";
        };

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
                        log.warn("잘못된 ID 형식으로 검색: {}", search);
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
                        log.warn("잘못된 리더 ID 형식으로 검색: {}", search);
                        teamPage = Page.empty(pageable);
                    }
                    break;
                case "org":
                    try {
                        Integer orgId = Integer.parseInt(search.trim());
                        teamPage = teamRepository.findByOrgId(orgId, pageable);
                    } catch (NumberFormatException e) {
                        log.warn("잘못된 조직 ID 형식으로 검색: {}", search);
                        teamPage = Page.empty(pageable);
                    }
                    break;
                default:
                    log.warn("알 수 없는 검색 기준: {}. 'all'을 기본값으로 사용합니다.", searchBy);
                    teamPage = teamRepository.findByAllFieldsContaining(search.trim(), pageable);
                    break;
            }
        }

        List<TeamResponseDTO> result = teamPage.getContent().stream()
                .map(TeamResponseDTO::fromEntity)
                .toList();

        log.info("총 {}개의 팀 중 {}개를 찾았습니다.", teamPage.getTotalElements(), result.size());
        return new PagedResponse<>(result, pageNum, pageSizeNum, teamPage.getTotalElements(), teamPage.getTotalPages());
    }

    /**
     * 팀에 참여합니다.
     * @param teamId 참여할 팀 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void joinTeam(Integer teamId, User user) {
        log.info("사용자 {}가 팀 {}에 참여합니다.", user.getId(), teamId);

        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + teamId));

        // TODO: 사용자가 이미 팀에 있는지 확인 (UserTeam 중간 테이블 사용)

        log.info("사용자 {}가 팀 {}에 성공적으로 참여했습니다.", user.getId(), teamId);
    }

    /**
     * 팀에서 나갑니다.
     * @param teamId 나갈 팀 ID
     * @param user 현재 로그인한 사용자 정보
     */
    public void leaveTeam(Integer teamId, User user) {
        log.info("사용자 {}가 팀 {}에서 나갑니다.", user.getId(), teamId);

        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다. ID: " + teamId));

        // TODO: 사용자가 팀에 있는지 확인하고 나가는 로직 구현
        // 리더는 리더십을 위임하기 전에는 나갈 수 없습니다.

        log.info("사용자 {}가 팀 {}에서 성공적으로 나갔습니다.", user.getId(), teamId);
    }
}
