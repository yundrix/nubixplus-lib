package com.nubixplus.lib.domain.entities.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.stereotype.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Optional;

/**
 * Tabla intermedia explicita entre {@link Role} y {@link Permission}.
 *
 * <p>La relacion N:M se modela con tres tablas ({@code roles} - {@code role_permissions}
 * - {@code permissions}) y esta entidad es la del medio: nada queda escondido detras
 * de un {@code @ManyToMany} del ORM.</p>
 *
 * <p>Extiende {@link BaseEntity} y no {@code AuditableEntity} a proposito: un vinculo
 * se otorga o se revoca, y no necesita ni autor ni version.</p>
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(
        name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_permissions",
                columnNames = {"role_id", "permission_id"}),
        indexes = {
                @Index(name = "ix_role_permissions_role", columnList = "role_id"),
                @Index(name = "ix_role_permissions_permission", columnList = "permission_id")
        }
)
public class RolePermission extends BaseEntity {

    public static final String FIELD_ROLE = "role";
    public static final String FIELD_PERMISSION = "permission";

    /** Lado propietario del vinculo. Se ignora en JSON para no ciclar con {@code Role}. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_permissions_role"))
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_role_permissions_permission"))
    private Permission permission;

    public static RolePermission of(Role role, Permission permission) {
        return RolePermission.builder().role(role).permission(permission).build();
    }

    /**
     * Identidad del vinculo: su clave natural (los dos ids), no el estado de las
     * entidades relacionadas. Si dependiera de ellas, editar el nombre de un rol
     * cambiaria el {@code hashCode} de filas ya guardadas en un {@code Set}.
     *
     * <p>Se nombran sin el prefijo {@code get} a proposito: como getters serian
     * propiedades JavaBean y Spring Data resolveria {@code findAllByRoleId} contra un
     * atributo {@code roleId} que no existe en el modelo, en vez de navegar
     * {@code role.id}. Leer el id de una asociacion perezosa no inicializa el proxy.</p>
     */
    @EqualsAndHashCode.Include
    public Long roleId() {
        return Optional.ofNullable(this.role).map(Role::getId).orElse(null);
    }

    @EqualsAndHashCode.Include
    public Long permissionId() {
        return Optional.ofNullable(this.permission).map(Permission::getId).orElse(null);
    }

    /**
     * {@code true} si el vinculo apunta al permiso indicado. Compara por id para no
     * inicializar el proxy perezoso; recae en {@code equals} cuando el permiso todavia
     * no fue persistido.
     */
    public boolean links(Permission other) {
        if (Objects.isNull(other)) {
            return false;
        }
        return Objects.nonNull(other.getId())
                ? other.getId().equals(this.permissionId())
                : other.equals(this.permission);
    }

    @Override
    public String toString() {
        return "RolePermission{" +
               "roleId=" + roleId() +
               ", permissionId=" + permissionId() +
               "} " + super.toString();
    }
}
