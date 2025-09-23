package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
import org.scit4bits.tonarinetserver.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자의 조직 내 역할 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    /**
     * 사용자의 전체 역할 정보를 조회합니다.
     * @param user
     * @return UserRole 객체 (존재하지 않으면 예외 발생)
     */
    public List<UserRole> getUserRoleByUser(User user) {
        return userRoleRepository.findByIdUserId(user.getId());
    }

    /**
     * 특정 조직 내에서 사용자의 역할을 확인합니다.
     * @param user 확인할 사용자
     * @param organization 확인할 조직
     * @param role 확인할 역할 (null인 경우, 멤버 여부만 확인)
     * @return 역할이 일치하면 true, 그렇지 않으면 false
     */
    public boolean checkUsersRoleInOrg(User user, Organization organization, String role) {

        UserRole userRole = userRoleRepository.findById(
                UserRole.UserRoleId.builder()
                        .userId(user.getId())
                        .orgId(organization.getId())
                        .build()
        ).orElse(null);

        if (userRole == null) {
            return false;
        }

        // 멤버십이 승인되었는지 확인
        if (!userRole.getIsGranted()) {
            return false;
        }

        // 특정 역할이 주어지지 않은 경우, 멤버 여부만 확인 (승인됨)
        // 역할이 주어진 경우, 해당 역할을 가지고 있는지 확인
        return role == null || userRole.getRole().equals(role);
    }
}
