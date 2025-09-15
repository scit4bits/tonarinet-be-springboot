package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Team;

import java.util.List;

/**
 * 팀 응답을 위한 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponseDTO {
    private Integer id;
    private String name;
    private Integer leaderUserId;
    private String leaderUserName;
    private Integer orgId;
    private String organizationName;
    private List<UserDTO> users;
    private Integer userCount;

    /**
     * Team 엔티티를 TeamResponseDTO로 변환합니다.
     * @param team 변환할 Team 엔티티
     * @return 변환된 TeamResponseDTO 객체
     */
    public static TeamResponseDTO fromEntity(Team team) {
        return TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .leaderUserId(team.getLeaderUserId())
                .leaderUserName(team.getLeaderUser() != null ? team.getLeaderUser().getName() : null)
                .orgId(team.getOrgId())
                .organizationName(team.getOrganization() != null ? team.getOrganization().getName() : null)
                .users(team.getUsers() != null ?
                        team.getUsers().stream().map(UserDTO::fromEntity).toList() : null)
                .userCount(team.getUsers() != null ? team.getUsers().size() : 0)
                .build();
    }
}
