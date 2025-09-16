package org.scit4bits.tonarinetserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.dto.ArticleDTO;
import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    /**
     * 모든 사용자 목록을 조회합니다. (관리자 전용)
     * @param user 현재 로그인한 사용자 정보
     * @return UserDTO 리스트
     */
    @GetMapping("/list")
    public ResponseEntity<List<UserDTO>> getUsers(@AuthenticationPrincipal User user) {
        if (user == null || !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 현재 로그인한 사용자의 정보를 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return UserDTO 형태의 사용자 정보
     */
    @GetMapping("/getMe")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal User user) {
        // Spring Security가 자동으로 인증된 사용자를 주입합니다.
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    /**
     * 사용자의 관리자 권한을 토글합니다. (관리자 전용)
     * @param user 현재 로그인한 사용자 정보 (관리자)
     * @param userId 권한을 변경할 사용자 ID
     * @return 성공 응답
     */
    @GetMapping("/toggleAdmin")
    public ResponseEntity<SimpleResponse> toggleAdmin(@AuthenticationPrincipal User user,
                                                      @RequestParam("userId") Integer userId) {
        if (!user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SimpleResponse("이 작업을 수행할 권한이 없습니다."));
        }
        try {
            userService.toggleAdmin(userId);
            return ResponseEntity.ok(new SimpleResponse("관리자 권한 변경에 성공했습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new SimpleResponse("관리자 권한 변경에 실패했습니다."));
        }
    }

    /**
     * 조직 내 사용자의 역할을 토글합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param userId 역할을 변경할 사용자 ID
     * @param orgId 조직 ID
     * @return 성공 응답
     */
    @GetMapping("/toggleGrant")
    public ResponseEntity<SimpleResponse> toggleGrant(@AuthenticationPrincipal User user,
                                                      @RequestParam("userId") Integer userId, @RequestParam("orgId") Integer orgId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.toggleUserRole(user, userId, orgId);
            return ResponseEntity.ok(new SimpleResponse("역할 변경에 성공했습니다."));
        } catch (Exception e) {
            log.debug("Error toggling grant: ", e);
            return ResponseEntity.status(400).body(new SimpleResponse("역할 변경에 실패했습니다."));
        }
    }

    /**
     * 조직 내 사용자의 역할을 변경합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param userId 역할을 변경할 사용자 ID
     * @param orgId 조직 ID
     * @param newRole 새로운 역할
     * @return 성공 응답
     */
    @GetMapping("changeRole")
    public ResponseEntity<SimpleResponse> changeRole(@AuthenticationPrincipal User user,
                                                     @RequestParam("userId") Integer userId, @RequestParam("orgId") Integer orgId,
                                                     @RequestParam("newRole") String newRole) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.changeUserRole(user, userId, orgId, newRole);
            return ResponseEntity.ok(new SimpleResponse("역할 변경에 성공했습니다."));
        } catch (Exception e) {
            log.debug("Error changing role: ", e);
            return ResponseEntity.status(400).body(new SimpleResponse("역할 변경에 실패했습니다."));
        }
    }

    /**
     * 사용자를 검색합니다. (관리자 전용)
     * @param searchBy 검색 기준 (all, username, nickname)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param user 현재 로그인한 사용자 정보 (관리자)
     * @return 페이징 처리된 UserDTO 리스트
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserDTO>> getUserSearch(
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal User user) {
        if (user == null || !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PagedResponse<UserDTO> users = userService.searchUser(searchBy, search, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }

    /**
     * 조직 내 사용자를 검색합니다. (관리자 전용)
     * @param orgId 조직 ID
     * @param searchBy 검색 기준 (all, username, nickname)
     * @param search 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @param user 현재 로그인한 사용자 정보 (관리자)
     * @return 페이징 처리된 UserDTO 리스트
     */
    @GetMapping("/searchWithOrg")
    public ResponseEntity<PagedResponse<UserDTO>> getUserSearchWithOrg(
            @RequestParam(name = "orgId", defaultValue = "") Integer orgId,
            @RequestParam(name = "searchBy", defaultValue = "all") String searchBy,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal User user) {
        if (user == null || !user.getIsAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PagedResponse<UserDTO> users = userService.searchOrganizationMembers(orgId, searchBy, search, page, pageSize,
                sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }

    /**
     * 현재 로그인한 사용자의 상담 내역을 조회합니다.
     * @param user 현재 로그인한 사용자 정보
     * @return ArticleDTO 리스트
     */
    @GetMapping("/mycounsels")
    public ResponseEntity<List<ArticleDTO>> getMyCounsels(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // 사용자의 상담 내역을 조회하여 반환합니다.
        List<ArticleDTO> counsels = userService.getMyCounsels(user);
        return ResponseEntity.ok(counsels);
    }

    /**
     * 사용자의 프로필 이미지를 변경합니다.
     * @param user 현재 로그인한 사용자 정보
     * @param fileId 새로운 프로필 이미지 파일 ID
     * @return 성공 응답
     */
    @PostMapping("/change-profile-image")
    public ResponseEntity<SimpleResponse> changeProfileImage(@AuthenticationPrincipal User user,
                                                            @RequestParam("fileId") Integer fileId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            userService.changeProfileImage(user, fileId);
            return ResponseEntity.ok(new SimpleResponse("프로필 이미지 변경에 성공했습니다."));
        } catch (Exception e) {
            log.debug("Error changing profile image: ", e);
            return ResponseEntity.status(400).body(new SimpleResponse("프로필 이미지 변경에 실패했습니다."));
        }
    }

}
