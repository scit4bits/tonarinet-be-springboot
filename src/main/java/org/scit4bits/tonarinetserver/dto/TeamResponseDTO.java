package org.scit4bits.tonarinetserver.dto;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
