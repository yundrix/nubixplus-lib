package com.nubixplus.lib.domain.entities.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.entities.user.User;
import com.nubixplus.lib.stereotype.BaseEntity;
import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

/**
 * Refresh token emitido en el login. Se guarda el HASH del token, nunca el valor
 * en claro, para poder revocarlo (logout) sin exponerlo si se filtra la tabla.
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
        name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_hash", columnNames = "token_hash"),
        indexes = {
                @Index(name = "ix_refresh_tokens_user", columnList = "user_id"),
                @Index(name = "ix_refresh_tokens_expires_at", columnList = "expires_at")
        }
)
public class RefreshToken extends BaseEntity {

    public static final String FIELD_TOKEN_HASH = "tokenHash";
    public static final String FIELD_USER = "user";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_REVOKED = "revoked";
    public static final String FIELD_EXPIRES_AT = "expiresAt";

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_tokens_organization"))
    private Organization organization;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Override
    public String toString() {
        return "RefreshToken{" +
               "revoked=" + revoked +
               ", expiresAt=" + expiresAt +
               "} " + super.toString();
    }
}
