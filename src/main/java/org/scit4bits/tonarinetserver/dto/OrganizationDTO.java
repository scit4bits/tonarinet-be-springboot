package org.scit4bits.tonarinetserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scit4bits.tonarinetserver.entity.Organization;

/**
 * 조직 정보를 전달하기 위한 DTO
 */
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
    private String role; // 직접 주입
    private Boolean isGranted; // 직접 주입
    private String entryMessage; // 직접 주입

    /**
     * Organization 엔티티를 OrganizationDTO로 변환합니다.
     *
     * @param organization 변환할 Organization 엔티티
     * @return 변환된 OrganizationDTO 객체
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