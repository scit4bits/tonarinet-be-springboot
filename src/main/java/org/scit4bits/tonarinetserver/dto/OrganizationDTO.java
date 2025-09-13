package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Organization;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDTO {
    private Integer id;
    private String name;
    private String description;
    private String countryCode;
    private String type;
    private String role;

    /**
     * Organization 엔티티를 OrganizationDTO로 변환하는 static 메서드
     *
     * @param organization Organization 엔티티
     * @return OrganizationDTO
     */
    public static OrganizationDTO fromEntity(Organization organization) {
        if (organization == null) {
            return null;
        }

        return OrganizationDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .countryCode(organization.getCountryCode())
                .type(organization.getType())
                .build();
    }

}