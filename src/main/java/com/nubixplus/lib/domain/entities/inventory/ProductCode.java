package com.nubixplus.lib.domain.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.ProductCodeType;
import com.nubixplus.lib.stereotype.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Codigo comercial de un producto: su SKU interno, sus codigos de barra y cualquier
 * otro identificador del negocio.
 *
 * <pre>
 * PRODUCT 1 ── N PRODUCT_CODE
 *                  |
 *                  +-- SKU    -> ABC-123
 *                  +-- UPC12  -> 012345678905
 *                  +-- EAN13  -> 8412345678901
 * </pre>
 *
 * <p><b>Por que una entidad y no columnas del producto.</b> Un articulo acumula
 * codigos con el tiempo (el del fabricante, el de la caja, el de una promocion) y no
 * se sabe de antemano cuantos ni de que tipo. Con columnas fijas cada tipo nuevo es
 * una migracion; con filas, es un INSERT.</p>
 *
 * <p><b>Unicidad: {@code (organization_id, type, code_value)}.</b> Dos decisiones
 * separadas:</p>
 * <ul>
 *   <li><b>Por compania, no global</b>: todo el catalogo es de cada tenant
 *       ({@code uk_suppliers_organization_document}, {@code uk_roles_organization_code}
 *       siguen el mismo criterio). Dos companias pueden vender el mismo articulo y
 *       registrar el mismo EAN.</li>
 *   <li><b>Tipo + valor, no solo valor</b>: cada tipo es un espacio de nombres
 *       distinto. Un SKU interno {@code 123456} y un CUSTOM {@code 123456} son
 *       identificadores de sistemas diferentes que coinciden por casualidad;
 *       prohibirlos obligaria a la compania a cambiar su codificacion interna por un
 *       choque que no significa nada. Dentro de un mismo tipo si es un duplicado
 *       real: no puede haber dos productos con el mismo EAN-13.</li>
 * </ul>
 *
 * <p>El valor va como texto y nunca como numero: un EAN puede empezar en cero y ese
 * cero es parte del codigo.</p>
 *
 * <p>Extiende {@link BaseEntity} y no {@code AuditableEntity} a proposito: un codigo
 * se agrega o se quita, y no necesita ni autor ni version.</p>
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(
        name = "product_codes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_codes_organization_type_value",
                columnNames = {"organization_id", "type", "code_value"}),
        indexes = {
                @Index(name = "ix_product_codes_product", columnList = "product_id"),
                @Index(name = "ix_product_codes_value", columnList = "code_value"),
                @Index(name = "ix_product_codes_type", columnList = "type")
        }
)
public class ProductCode extends BaseEntity {

    public static final String FIELD_PRODUCT = "product";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_VALUE = "value";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_codes_product"))
    private Product product;

    /**
     * Se guarda tambien en el codigo, desnormalizada desde el producto, porque es la
     * columna lider de la restriccion de unicidad por compania. Es el mismo criterio
     * de {@code Category}, que lleva {@code family_id} y {@code organization_id}.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_codes_organization"))
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProductCodeType type;

    /**
     * El valor ya normalizado. La columna se llama {@code code_value} porque
     * {@code VALUE} es palabra reservada en H2 y habria que citarla en cada motor.
     */
    @Column(name = "code_value", nullable = false, length = 60)
    private String value;

    public static ProductCode of(Product product, ProductCodeType type, String value) {
        return ProductCode.builder()
                .product(product)
                .organization(Objects.isNull(product) ? null : product.getOrganization())
                .type(type)
                .value(value)
                .build();
    }

    /**
     * Identidad por clave natural (producto + tipo + valor). Se nombra sin el prefijo
     * {@code get} para que Spring Data no lo tome por un atributo del modelo. Ver
     * {@code RolePermission}.
     */
    @EqualsAndHashCode.Include
    public Long productId() {
        return Optional.ofNullable(this.product).map(Product::getId).orElse(null);
    }

    @EqualsAndHashCode.Include
    public ProductCodeType type() {
        return this.type;
    }

    @EqualsAndHashCode.Include
    public String value() {
        return this.value;
    }

    /** {@code true} si el codigo es de ese tipo con ese valor. */
    public boolean matches(ProductCodeType otherType, String otherValue) {
        return this.type == otherType && Objects.equals(this.value, otherValue);
    }

    @Override
    public String toString() {
        return "ProductCode{" +
               "type=" + type +
               ", value='" + value + '\'' +
               "} " + super.toString();
    }
}
