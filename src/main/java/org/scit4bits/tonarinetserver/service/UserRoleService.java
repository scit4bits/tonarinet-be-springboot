package org.scit4bits.tonarinetserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scit4bits.tonarinetserver.entity.Organization;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.entity.UserRole;
import org.scit4bits.tonarinetserver.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    public boolean checkUsersRoleInOrg(User user, Organization organization, String role) {

        UserRole userRole = userRoleRepository.findById(
                UserRole.UserRoleId.builder()
                        .userId(user.getId())
                        .orgId(organization.getId())
                        .build()
        ).get();

        if (!userRole.getIsGranted()) {
            return false;
        }

        return role == null || userRole.getRole().equals(role);
    }
}
