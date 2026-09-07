package com.nubixplus.lib.domain.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.DocumentType;
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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Suplidor de la compania. Se identifica por su RNC o cedula, igual que la
 * {@link Organization}, y se normaliza con {@code Documents} antes de guardarlo.
 *
 * <p>El documento es unico DENTRO de la compania, no en la plataforma: dos companias
 * pueden comprarle al mismo proveedor y cada una lleva su propia ficha.</p>
 *
 * <p>La relacion con {@link Product} es N:M y esta modelada con tres tablas a traves
 * de {@link ProductSupplier}.</p>
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
        name = "suppliers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_suppliers_organization_code",
                        columnNames = {"organization_id", "code"}),
                @UniqueConstraint(name = "uk_suppliers_organization_document",
                        columnNames = {"organization_id", "document_number"})
        },
        indexes = {
                @Index(name = "ix_suppliers_organization", columnList = "organization_id"),
                @Index(name = "ix_suppliers_document_number", columnList = "document_number"),
                @Index(name = "ix_suppliers_active", columnList = "active")
        }
)
public class Supplier extends AuditableEntity {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_LEGAL_NAME = "legalName";
    public static final String FIELD_COMMERCIAL_NAME = "commercialName";
    public static final String FIELD_DOCUMENT_TYPE = "documentType";
    public static final String FIELD_DOCUMENT_NUMBER = "documentNumber";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_PRODUCT_SUPPLIERS = "productSuppliers";

    /** Identificador corto en MAYUSCULAS, unico por compania. Ej. SUP-001. */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "commercial_name", length = 200)
    private String commercialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20)
    private DocumentType documentType;

    /** Normalizado (solo digitos), igual que en {@code Organization}. */
    @Column(name = "document_number", length = 20)
    private String documentNumber;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", length = 300)
    private String address;

    /** Persona de contacto en el suplidor. */
    @Column(name = "contact_name", length = 150)
    private String contactName;

    /** Dias de credito acordados. */
    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_suppliers_organization"))
    private Organization organization;

    /**
     * Disponibilidad del suplidor: si esta inactivo no se le emiten ordenes. Es
     * binaria, como en el resto del catalogo.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    /** Lado inverso de la N:M con {@link Product}. Ver {@link ProductSupplier}. */
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = ProductSupplier.FIELD_SUPPLIER,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<ProductSupplier> productSuppliers = new LinkedHashSet<>();

    /** Nombre comercial si existe; si no, la razon social. */
    public String getDisplayName() {
        return Objects.nonNull(commercialName) && !commercialName.isBlank() ? commercialName : legalName;
    }

    @Override
    public String toString() {
        return "Supplier{" +
               "code='" + code + '\'' +
               ", legalName='" + legalName + '\'' +
               ", active=" + active +
               "} " + super.toString();
    }
}
