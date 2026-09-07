package com.nubixplus.lib.domain.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.auth.Role;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.MembershipStatus;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Membresia: vincula un usuario con una organizacion y le asigna los roles que
 * tiene DENTRO de esa organizacion. Es el nucleo del modelo multi-organizacion.
 *
 * <p>La relacion con {@link Role} es N:M y esta modelada con tres tablas a traves de
 * {@link UserOrganizationRole}. {@link #getRoles()} y {@link #setRoles(Collection)}
 * son vistas de conveniencia sobre esos vinculos.</p>
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
        name = "user_organizations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_organizations_user_org",
                columnNames = {"user_id", "organization_id"}),
        indexes = {
                @Index(name = "ix_user_organizations_user", columnList = "user_id"),
                @Index(name = "ix_user_organizations_organization", columnList = "organization_id")
        }
)
public class UserOrganization extends AuditableEntity {

    public static final String FIELD_USER = "user";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_JOB_TITLE = "jobTitle";
    public static final String FIELD_DEFAULT_ORGANIZATION = "defaultOrganization";
    public static final String FIELD_USER_ROLES = "userRoles";

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_organizations_user"))
    private User user;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_organizations_organization"))
    private Organization organization;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    /**
     * Organizacion sugerida por defecto para este usuario. La columna se llama igual
     * que el atributo: el resto de los booleanos del modelo ({@code system_role},
     * {@code email_verified}) tampoco usan el prefijo {@code is_}.
     */
    @Column(name = "default_organization", nullable = false)
    private boolean defaultOrganization;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "job_title", length = 120)
    private String jobTitle;

    /** Vinculos con los roles de la membresia. Ver {@link UserOrganizationRole}. */
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = UserOrganizationRole.FIELD_USER_ORGANIZATION,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<UserOrganizationRole> userRoles = new LinkedHashSet<>();

    /** Roles de la membresia, resueltos desde la tabla intermedia. */
    public Set<Role> getRoles() {
        return this.userRoles.stream()
                .map(UserOrganizationRole::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Reemplaza los roles de la membresia sincronizando la tabla intermedia: quita los
     * vinculos que sobran y agrega los que faltan, sin recrear los que ya existian.
     */
    public void setRoles(Collection<Role> roles) {
        final Set<Role> target = Objects.isNull(roles)
                ? Set.of()
                : roles.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        this.userRoles.removeIf(link -> target.stream().noneMatch(link::links));
        target.stream().filter(role -> !this.hasRole(role)).forEach(this::addRole);
    }

    public void addRole(Role role) {
        if (Objects.isNull(role) || this.hasRole(role)) {
            return;
        }
        this.userRoles.add(UserOrganizationRole.of(this, role));
    }

    public void removeRole(Role role) {
        this.userRoles.removeIf(link -> link.links(role));
    }

    public boolean hasRole(Role role) {
        return this.userRoles.stream().anyMatch(link -> link.links(role));
    }

    public boolean canOperate() {
        return Objects.nonNull(status) && status.canOperate();
    }

    /** Codigos de rol, listos para convertirse en authorities. */
    public Set<String> getRoleCodes() {
        return this.getRoles().stream().map(Role::getCode).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Union de los permisos de todos los roles de la membresia. */
    public Set<String> getPermissionCodes() {
        return this.getRoles().stream()
                .flatMap(role -> role.getPermissionCodes().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean hasRole(String code) {
        return this.getRoles().stream().anyMatch(role -> role.getCode().equals(code));
    }

    @Override
    public String toString() {
        return "UserOrganization{" +
               "status=" + status +
               ", jobTitle='" + jobTitle + '\'' +
               "} " + super.toString();
    }
}
