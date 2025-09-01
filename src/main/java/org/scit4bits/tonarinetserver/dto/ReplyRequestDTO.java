package org.scit4bits.tonarinetserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyRequestDTO {
    
    @NotBlank(message = "Contents is required")
    private String contents;
    
    @NotNull(message = "Article ID is required")  
    private Integer articleId;
}
