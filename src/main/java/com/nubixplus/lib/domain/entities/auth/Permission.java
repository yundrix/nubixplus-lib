package com.nubixplus.lib.domain.entities.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Permiso atomico del sistema. El {@code code} tiene el formato {@code modulo:accion}
 * (ej. {@code users:create}) y es lo que se evalua en {@code @PreAuthorize}.
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(
        name = "permissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_permissions_code", columnNames = "code"),
        indexes = @Index(name = "ix_permissions_module", columnList = "module")
)
public class Permission extends AuditableEntity {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_MODULE = "module";
    public static final String FIELD_ACTION = "action";

    public static final String CODE_SEPARATOR = ":";

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    /** Agrupador funcional: users, roles, organizations, invoices, ... */
    @Column(name = "module", nullable = false, length = 60)
    private String module;

    /** read, create, update, delete, approve, ... */
    @Column(name = "action", nullable = false, length = 60)
    private String action;

    /** Construye el permiso a partir de un codigo {@code modulo:accion}. */
    public static Permission of(String code, String name, String description) {
        final String[] parts = code.split(CODE_SEPARATOR, 2);
        return Permission.builder()
                .code(code)
                .module(parts[0])
                .action(parts[1])
                .name(name)
                .description(description)
                .build();
    }

    @Override
    public String toString() {
        return "Permission{" +
               "code='" + code + '\'' +
               "} " + super.toString();
    }
}
