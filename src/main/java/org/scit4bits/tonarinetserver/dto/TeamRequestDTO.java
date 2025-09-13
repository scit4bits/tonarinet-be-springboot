package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamRequestDTO {

    @NotBlank(message = "Team name is required")
    private String name;

    @NotNull(message = "Organization ID is required")
    private Integer orgId;

    private List<UserDTO> members;
}
