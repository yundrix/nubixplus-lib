package com.nubixplus.lib.domain.entities.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.constants.AppConstants;
import com.nubixplus.lib.domain.types.DocumentType;
import com.nubixplus.lib.domain.types.OrganizationStatus;
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

import java.util.Objects;

/**
 * Compania / tenant. Se identifica hacia afuera por su RNC o cedula, que es
 * justamente lo que viaja en el login y en el header {@code X-Company-Document}.
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
        name = "organizations",
        uniqueConstraints = @UniqueConstraint(name = "uk_organizations_document_number", columnNames = "document_number"),
        indexes = {
                @Index(name = "ix_organizations_document_number", columnList = "document_number"),
                @Index(name = "ix_organizations_status", columnList = "status")
        }
)
public class Organization extends AuditableEntity {

    public static final String FIELD_LEGAL_NAME = "legalName";
    public static final String FIELD_COMMERCIAL_NAME = "commercialName";
    public static final String FIELD_DOCUMENT_TYPE = "documentType";
    public static final String FIELD_DOCUMENT_NUMBER = "documentNumber";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_STATUS = "status";

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "commercial_name", length = 200)
    private String commercialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    /** Siempre normalizado (solo digitos). Ver {@code Documents#normalize}. */
    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", length = 300)
    private String address;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @Builder.Default
    @Column(name = "timezone", length = 60)
    private String timezone = AppConstants.DEFAULT_TIMEZONE;

    @Builder.Default
    @Column(name = "currency", length = 3)
    private String currency = AppConstants.DEFAULT_CURRENCY;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /** Nombre comercial si existe; si no, la razon social. */
    public String getDisplayName() {
        return Objects.nonNull(commercialName) && !commercialName.isBlank() ? commercialName : legalName;
    }

    public boolean isOperational() {
        return Objects.nonNull(status) && status.isOperational();
    }

    @Override
    public String toString() {
        return "Organization{" +
               "legalName='" + legalName + '\'' +
               ", documentNumber='" + documentNumber + '\'' +
               ", status=" + status +
               "} " + super.toString();
    }
}
