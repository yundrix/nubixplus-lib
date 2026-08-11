package com.nubixplus.lib.domain.dtos.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.DocumentType;
import com.nubixplus.lib.domain.types.OrganizationStatus;
import com.nubixplus.lib.utils.Documents;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * Datos minimos de la compania. Es lo que se embebe en la respuesta del login y en
 * los listados donde no hace falta el detalle completo.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseOrganizationDTO extends BaseDTO {

    private String name;
    private DocumentType documentType;
    private String documentNumber;
    private String documentNumberFormatted;
    private OrganizationStatus status;

    public BaseOrganizationDTO(Organization organization) {
        super(organization);
        if (Objects.nonNull(organization)) {
            this.name = organization.getDisplayName();
            this.documentType = organization.getDocumentType();
            this.documentNumber = organization.getDocumentNumber();
            this.documentNumberFormatted = Documents.format(organization.getDocumentNumber());
            this.status = organization.getStatus();
        }
    }

    public static BaseOrganizationDTO of(Organization organization) {
        return Optional.ofNullable(organization).map(BaseOrganizationDTO::new).orElse(null);
    }
}
