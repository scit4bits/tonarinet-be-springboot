package org.scit4bits.tonarinetserver.repository;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 사용자-역할(UserRole) 엔티티에 대한 데이터 액세스 작업을 처리하는 리포지토리
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {

    /**
     * 특정 조직 ID와 역할로 사용자 역할 목록을 조회합니다.
     * @param orgId 조직 ID
     * @param role 역할
     * @return 해당 조건에 맞는 UserRole 목록
     */
    List<UserRole> findByIdOrgIdAndRole(Integer orgId, String role);

    /**
     * 특정 조직 ID, 역할, 승인 상태로 사용자 역할 목록을 조회합니다.
     * @param orgId 조직 ID
     * @param role 역할
     * @param isGranted 승인 상태
     * @return 해당 조건에 맞는 UserRole 목록
     */
    List<UserRole> findByIdOrgIdAndRoleAndIsGranted(Integer orgId, String role, Boolean isGranted);

    /**
     * 특정 조직 ID로 사용자 역할 목록을 조회합니다.
     * @param orgId 조직 ID
     * @return 해당 조직의 모든 UserRole 목록
     */
    List<UserRole> findByIdOrgId(Integer orgId);

    /**
     * 특정 사용자 ID와 조직 ID로 사용자 역할을 조회합니다.
     * @param userId 사용자 ID
     * @param orgId 조직 ID
     * @return 해당 조건에 맞는 UserRole (Optional로 감싸져 반환됨)
     */
    List<UserRole> findByIdUserIdAndIdOrgId(Integer userId, Integer orgId);

}
