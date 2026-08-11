package com.nubixplus.lib.domain.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.auth.Permission;
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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDTO extends BaseDTO {

    private String code;
    private String name;
    private String description;
    private String module;
    private String action;

    protected PermissionDTO(Permission permission) {
        super(permission);
        this.code = permission.getCode();
        this.name = permission.getName();
        this.description = permission.getDescription();
        this.module = permission.getModule();
        this.action = permission.getAction();
    }

    public static PermissionDTO of(Permission permission) {
        return Optional.ofNullable(permission).map(PermissionDTO::new).orElse(null);
    }

    /** Ordenados por codigo para que el front siempre reciba la misma secuencia. */
    public static List<PermissionDTO> of(Collection<Permission> permissions) {
        if (Objects.isNull(permissions)) {
            return List.of();
        }
        return permissions.stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(PermissionDTO::of)
                .toList();
    }
}
