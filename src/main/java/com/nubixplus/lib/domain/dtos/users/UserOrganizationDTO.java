package com.nubixplus.lib.domain.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.entities.user.UserOrganization;
import com.nubixplus.lib.domain.types.MembershipStatus;
import com.nubixplus.lib.utils.Documents;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Una de las companias a las que pertenece el usuario. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserOrganizationDTO extends BaseDTO {

    private Long membershipId;
    private Long organizationId;
    private String organizationName;
    private String documentNumber;
    private String documentNumberFormatted;
    private MembershipStatus status;
    private boolean defaultOrganization;

    protected UserOrganizationDTO(UserOrganization membership) {
        super(membership);
        final Organization organization = membership.getOrganization();
        this.membershipId = membership.getId();
        this.organizationId = organization.getId();
        this.organizationName = organization.getDisplayName();
        this.documentNumber = organization.getDocumentNumber();
        this.documentNumberFormatted = Documents.format(organization.getDocumentNumber());
        this.status = membership.getStatus();
        this.defaultOrganization = membership.isDefaultOrganization();
    }

    public static UserOrganizationDTO of(UserOrganization membership) {
        return Optional.ofNullable(membership).map(UserOrganizationDTO::new).orElse(null);
    }

    public static List<UserOrganizationDTO> of(Collection<UserOrganization> memberships) {
        if (Objects.isNull(memberships)) {
            return List.of();
        }
        return memberships.stream().map(UserOrganizationDTO::of).toList();
    }
}
