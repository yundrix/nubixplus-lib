package com.nubixplus.lib.domain.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.auth.Role;
import com.nubixplus.lib.domain.entities.organization.Organization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseRoleDTO extends BaseDTO {

    private String code;
    private String name;
    private String description;
    private boolean systemRole;
    private Long organizationId;

    public BaseRoleDTO(Role role) {
        super(role);
        if (Objects.nonNull(role)) {
            this.code = role.getCode();
            this.name = role.getName();
            this.description = role.getDescription();
            this.systemRole = role.isSystemRole();
            this.organizationId = Optional.ofNullable(role.getOrganization()).map(Organization::getId).orElse(null);
        }
    }

    public static BaseRoleDTO of(Role role) {
        return Optional.ofNullable(role).map(BaseRoleDTO::new).orElse(null);
    }
}
