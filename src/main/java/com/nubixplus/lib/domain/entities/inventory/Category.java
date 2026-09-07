package com.nubixplus.lib.domain.entities.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.stereotype.AuditableEntity;
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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Categoria: el segundo nivel de la clasificacion, siempre dentro de una
 * {@link Family}.
 *
 * <p>La relacion con la familia es N:1 (una familia tiene muchas categorias, una
 * categoria pertenece a una sola familia): no es una N:M, asi que no lleva tabla
 * intermedia. Es obligatoria, porque toda categoria se reporta bajo su linea.</p>
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
        name = "categories",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_categories_organization_code",
                columnNames = {"organization_id", "code"}),
        indexes = {
                @Index(name = "ix_categories_organization", columnList = "organization_id"),
                @Index(name = "ix_categories_family", columnList = "family_id"),
                @Index(name = "ix_categories_code", columnList = "code"),
                @Index(name = "ix_categories_active", columnList = "active")
        }
)
public class Category extends AuditableEntity {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_FAMILY = "family";
    public static final String FIELD_ORGANIZATION = "organization";

    /** Identificador corto en MAYUSCULAS, unico por compania. Ej. REF. */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_categories_family"))
    private Family family;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_categories_organization"))
    private Organization organization;

    /**
     * Disponibilidad funcional. Es binaria, asi que va como boolean y no como enum
     * de dos valores.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return "Category{" +
               "code='" + code + '\'' +
               ", name='" + name + '\'' +
               "} " + super.toString();
    }
}
