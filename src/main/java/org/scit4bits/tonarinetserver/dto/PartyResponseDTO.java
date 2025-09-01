package org.scit4bits.tonarinetserver.dto;

import java.util.List;

import org.scit4bits.tonarinetserver.entity.Party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyResponseDTO {
    private Integer id;
    private String name;
    private Integer leaderUserId;
    private String leaderUserName;
    private List<UserDTO> users;
    private Integer userCount;

    public static PartyResponseDTO fromEntity(Party party) {
        return PartyResponseDTO.builder()
                .id(party.getId())
                .name(party.getName())
                .leaderUserId(party.getLeaderUserId())
                .leaderUserName(party.getLeaderUser() != null ? party.getLeaderUser().getName() : null)
                .users(party.getUsers() != null ? 
                    party.getUsers().stream().map(UserDTO::fromEntity).toList() : null)
                .userCount(party.getUsers() != null ? party.getUsers().size() : 0)
                .build();
    }
}
