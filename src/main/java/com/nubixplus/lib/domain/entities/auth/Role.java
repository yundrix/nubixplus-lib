package com.nubixplus.lib.domain.entities.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Rol asignable dentro de una organizacion.
 *
 * <p>Si {@code organization} es {@code null} el rol es de sistema y esta disponible
 * para todas las organizaciones (ej. ORG_ADMIN). Si tiene organizacion, es un rol
 * propio creado por esa compania.</p>
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
        name = "roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_roles_organization_code",
                columnNames = {"organization_id", "code"}),
        indexes = {
                @Index(name = "ix_roles_code", columnList = "code"),
                @Index(name = "ix_roles_organization", columnList = "organization_id")
        }
)
public class Role extends AuditableEntity {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_SYSTEM_ROLE = "systemRole";
    public static final String FIELD_PERMISSIONS = "permissions";

    /** Identificador estable en MAYUSCULAS, ej. ORG_ADMIN. */
    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    /** {@code null} = rol de sistema compartido por todas las organizaciones. */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_roles_organization"))
    private Organization organization;

    /** Los roles de sistema no se pueden editar ni borrar desde la API. */
    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permissions",
                    columnNames = {"role_id", "permission_id"})
    )
    private Set<Permission> permissions = new LinkedHashSet<>();

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public boolean isGlobal() {
        return Objects.isNull(organization);
    }

    @Override
    public String toString() {
        return "Role{" +
               "code='" + code + '\'' +
               ", systemRole=" + systemRole +
               "} " + super.toString();
    }
}
