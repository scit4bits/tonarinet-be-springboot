package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartyRequestDTO {

    @NotBlank(message = "Party name is required")
    private String name;
}
