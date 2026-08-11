package com.nubixplus.lib.domain.entities.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.auth.Permission;
import com.nubixplus.lib.domain.entities.auth.Role;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.MembershipStatus;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Membresia: vincula un usuario con una organizacion y le asigna los roles que
 * tiene DENTRO de esa organizacion. Es el nucleo del modelo multi-organizacion.
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
    public static final String FIELD_ROLES = "roles";

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

    /** Organizacion sugerida por defecto para este usuario. */
    @Column(name = "is_default", nullable = false)
    private boolean defaultOrganization;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "job_title", length = 120)
    private String jobTitle;

    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_organization_roles",
            joinColumns = @JoinColumn(name = "user_organization_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_user_organization_roles",
                    columnNames = {"user_organization_id", "role_id"})
    )
    private Set<Role> roles = new LinkedHashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean canOperate() {
        return Objects.nonNull(status) && status.canOperate();
    }

    /** Codigos de rol, listos para convertirse en authorities. */
    public Set<String> getRoleCodes() {
        return roles.stream().map(Role::getCode).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Union de los permisos de todos los roles de la membresia. */
    public Set<String> getPermissionCodes() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean hasRole(String code) {
        return roles.stream().anyMatch(role -> role.getCode().equals(code));
    }

    @Override
    public String toString() {
        return "UserOrganization{" +
               "status=" + status +
               ", jobTitle='" + jobTitle + '\'' +
               "} " + super.toString();
    }
}
