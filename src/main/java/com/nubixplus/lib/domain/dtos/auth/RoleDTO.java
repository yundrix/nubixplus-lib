package com.nubixplus.lib.domain.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Rol con su catalogo de permisos. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO extends BaseRoleDTO {

    private List<PermissionDTO> permissions;

    protected RoleDTO(Role role) {
        super(role);
        this.permissions = PermissionDTO.of(role.getPermissions());
    }

    public static RoleDTO of(Role role) {
        return Optional.ofNullable(role).map(RoleDTO::new).orElse(null);
    }

    public static List<RoleDTO> of(Collection<Role> roles) {
        if (Objects.isNull(roles)) {
            return List.of();
        }
        return roles.stream()
                .sorted(Comparator.comparing(Role::getCode))
                .map(RoleDTO::of)
                .toList();
    }
}
