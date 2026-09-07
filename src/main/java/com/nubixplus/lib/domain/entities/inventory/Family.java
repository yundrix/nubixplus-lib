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
 * Familia: el nivel mas alto de la clasificacion del catalogo.
 *
 * <p>Jerarquia del modulo: {@code Familia 1:N Categoria 1:N Producto}. La familia
 * agrupa categorias afines (ej. "Bebidas" agrupa "Refrescos" y "Jugos") y es lo que
 * se usa para los reportes por linea de negocio.</p>
 *
 * <p>Como todo el catalogo, pertenece a una compania: el {@code code} es unico
 * dentro de ella, no en toda la plataforma.</p>
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
        name = "families",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_families_organization_code",
                columnNames = {"organization_id", "code"}),
        indexes = {
                @Index(name = "ix_families_organization", columnList = "organization_id"),
                @Index(name = "ix_families_code", columnList = "code"),
                @Index(name = "ix_families_active", columnList = "active")
        }
)
public class Family extends AuditableEntity {

    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_ORGANIZATION = "organization";

    /** Identificador corto en MAYUSCULAS, unico por compania. Ej. BEB. */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_families_organization"))
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
        return "Family{" +
               "code='" + code + '\'' +
               ", name='" + name + '\'' +
               "} " + super.toString();
    }
}
