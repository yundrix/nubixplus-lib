package com.nubixplus.lib.domain.dtos.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationDTO extends BaseOrganizationDTO {

    private String legalName;
    private String commercialName;
    private String email;
    private String phone;
    private String address;
    private String logoUrl;

    protected OrganizationDTO(Organization organization) {
        super(organization);
        this.legalName = organization.getLegalName();
        this.commercialName = organization.getCommercialName();
        this.email = organization.getEmail();
        this.phone = organization.getPhone();
        this.address = organization.getAddress();
        this.logoUrl = organization.getLogoUrl();
    }

    public static OrganizationDTO of(Organization organization) {
        return Optional.ofNullable(organization).map(OrganizationDTO::new).orElse(null);
    }
}
