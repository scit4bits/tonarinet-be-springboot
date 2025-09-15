package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Party;

import java.util.List;

/**
 * 파티 응답을 위한 DTO
 */
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

    /**
     * Party 엔티티를 PartyResponseDTO로 변환합니다.
     * @param party 변환할 Party 엔티티
     * @return 변환된 PartyResponseDTO 객체
     */
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
