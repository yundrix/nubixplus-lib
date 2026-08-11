package com.nubixplus.lib.domain.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.types.UserStatus;
import com.nubixplus.lib.stereotype.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
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
import java.util.Objects;

/**
 * Usuario de la plataforma. La identidad (correo + contrasena) es global: el mismo
 * usuario puede pertenecer a varias organizaciones a traves de {@link UserOrganization}.
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
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        indexes = {
                @Index(name = "ix_users_email", columnList = "email"),
                @Index(name = "ix_users_status", columnList = "status")
        }
)
public class User extends AuditableEntity {

    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_STATUS = "status";

    /** Se persiste siempre en minusculas. */
    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 30)
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "locale", length = 10)
    private String locale;

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }

    /** Bloqueo temporal vigente por intentos fallidos. */
    public boolean isTemporarilyLocked() {
        return Objects.nonNull(lockedUntil) && lockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean canLogin() {
        return Objects.nonNull(status) && status.canLogin();
    }

    /** Deja la cuenta lista para operar despues de un login correcto. */
    public void registerSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
               "email='" + email + '\'' +
               ", status=" + status +
               "} " + super.toString();
    }
}
