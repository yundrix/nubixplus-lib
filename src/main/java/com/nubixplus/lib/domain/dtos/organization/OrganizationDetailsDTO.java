package com.nubixplus.lib.domain.dtos.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

/** Vista completa de la compania: agrega la configuracion regional. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationDetailsDTO extends OrganizationDTO {

    private String timezone;
    private String currency;

    protected OrganizationDetailsDTO(Organization organization) {
        super(organization);
        this.timezone = organization.getTimezone();
        this.currency = organization.getCurrency();
    }

    public static OrganizationDetailsDTO of(Organization organization) {
        return Optional.ofNullable(organization).map(OrganizationDetailsDTO::new).orElse(null);
    }
}
