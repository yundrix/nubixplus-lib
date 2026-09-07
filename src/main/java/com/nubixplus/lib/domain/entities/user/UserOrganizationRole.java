package com.nubixplus.lib.domain.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.auth.Role;
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
 * Tabla intermedia explicita entre {@link UserOrganization} y {@link Role}: los roles
 * se otorgan POR MEMBRESIA, asi que alguien puede ser ORG_ADMIN en una compania y
 * VIEWER en otra.
 *
 * <p>Tres tablas ({@code user_organizations} - {@code user_organization_roles} -
 * {@code roles}) y ningun {@code @ManyToMany} implicito. Ver {@code RolePermission}
 * para el porque de extender {@link BaseEntity} y no {@code AuditableEntity}.</p>
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
        name = "user_organization_roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_organization_roles",
                columnNames = {"user_organization_id", "role_id"}),
        indexes = {
                @Index(name = "ix_user_organization_roles_member", columnList = "user_organization_id"),
                @Index(name = "ix_user_organization_roles_role", columnList = "role_id")
        }
)
public class UserOrganizationRole extends BaseEntity {

    public static final String FIELD_USER_ORGANIZATION = "userOrganization";
    public static final String FIELD_ROLE = "role";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_organization_roles_member"))
    private UserOrganization userOrganization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_organization_roles_role"))
    private Role role;

    public static UserOrganizationRole of(UserOrganization membership, Role role) {
        return UserOrganizationRole.builder().userOrganization(membership).role(role).build();
    }

    /** Identidad por clave natural (los dos ids). Ver {@code RolePermission}. */
    @EqualsAndHashCode.Include
    public Long userOrganizationId() {
        return Optional.ofNullable(this.userOrganization).map(UserOrganization::getId).orElse(null);
    }

    @EqualsAndHashCode.Include
    public Long roleId() {
        return Optional.ofNullable(this.role).map(Role::getId).orElse(null);
    }

    /** Ver {@code RolePermission#links}: compara por id para no inicializar el proxy. */
    public boolean links(Role other) {
        if (Objects.isNull(other)) {
            return false;
        }
        return Objects.nonNull(other.getId())
                ? other.getId().equals(this.roleId())
                : other.equals(this.role);
    }

    @Override
    public String toString() {
        return "UserOrganizationRole{" +
               "userOrganizationId=" + userOrganizationId() +
               ", roleId=" + roleId() +
               "} " + super.toString();
    }
}
