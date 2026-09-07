package com.nubixplus.lib.domain.entities.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rol asignable dentro de una organizacion.
 *
 * <p>Si {@code organization} es {@code null} el rol es de sistema y esta disponible
 * para todas las organizaciones (ej. ORG_ADMIN). Si tiene organizacion, es un rol
 * propio creado por esa compania.</p>
 *
 * <p>La relacion con {@link Permission} es N:M y esta modelada con tres tablas a
 * traves de {@link RolePermission}. {@link #getPermissions()} y
 * {@link #setPermissions(Collection)} son vistas de conveniencia sobre esos
 * vinculos: la coleccion que persiste es {@code rolePermissions}.</p>
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
    public static final String FIELD_ROLE_PERMISSIONS = "rolePermissions";

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

    /**
     * Vinculos con el catalogo de permisos. {@code orphanRemoval} es lo que convierte
     * el quitar un permiso de la coleccion en un DELETE de la fila intermedia.
     */
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = RolePermission.FIELD_ROLE,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    /** Permisos del rol, resueltos desde la tabla intermedia. */
    public Set<Permission> getPermissions() {
        return this.rolePermissions.stream()
                .map(RolePermission::getPermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Reemplaza los permisos del rol sincronizando la tabla intermedia: elimina los
     * vinculos que sobran y agrega los que faltan, sin recrear los que ya existian.
     */
    public void setPermissions(Collection<Permission> permissions) {
        final Set<Permission> target = Objects.isNull(permissions)
                ? Set.of()
                : permissions.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        this.rolePermissions.removeIf(link -> target.stream().noneMatch(link::links));
        target.stream().filter(permission -> !this.hasPermission(permission)).forEach(this::addPermission);
    }

    public void addPermission(Permission permission) {
        if (Objects.isNull(permission) || this.hasPermission(permission)) {
            return;
        }
        this.rolePermissions.add(RolePermission.of(this, permission));
    }

    public void removePermission(Permission permission) {
        this.rolePermissions.removeIf(link -> link.links(permission));
    }

    public boolean hasPermission(Permission permission) {
        return this.rolePermissions.stream().anyMatch(link -> link.links(permission));
    }

    /** Codigos de permiso del rol, ej. {@code users:create}. */
    public Set<String> getPermissionCodes() {
        return this.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
