package com.nubixplus.lib.domain.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.domain.types.ProductCodeType;
import com.nubixplus.lib.domain.types.ProductStatus;
import com.nubixplus.lib.domain.types.UnitOfMeasure;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Producto del catalogo de inventario.
 *
 * <p><b>Codigos.</b> No son columnas del producto: viven en {@link ProductCode}, con
 * una relacion 1:N. Un articulo acumula codigos de distinto tipo (su SKU interno, el
 * UPC del fabricante, el EAN de la caja) y no se sabe de antemano cuantos ni de que
 * clase, asi que cada tipo nuevo es una fila y no una migracion.</p>
 *
 * <pre>
 * Product 1 ── N ProductCode  (SKU, UPC12, EAN13, ...)
 * </pre>
 *
 * <p>La relacion con {@link Supplier} es N:M y esta modelada con tres tablas a traves
 * de {@link ProductSupplier}.</p>
 *
 * <p>La {@link Brand} es N:1 y opcional: se registra una vez y se referencia, en vez
 * de repetir el texto en cada producto. Hay articulos genericos que no llevan.</p>
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
        name = "products",
        indexes = {
                @Index(name = "ix_products_organization", columnList = "organization_id"),
                @Index(name = "ix_products_category", columnList = "category_id"),
                @Index(name = "ix_products_brand", columnList = "brand_id"),
                @Index(name = "ix_products_name", columnList = "name"),
                @Index(name = "ix_products_status", columnList = "status")
        }
)
public class Product extends AuditableEntity {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_BRAND = "brand";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_ORGANIZATION = "organization";
    public static final String FIELD_CODES = "codes";
    public static final String FIELD_PRODUCT_SUPPLIERS = "productSuppliers";

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /** Opcional: un articulo generico o de produccion propia no tiene marca. */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id",
            foreignKey = @ForeignKey(name = "fk_products_brand"))
    private Brand brand;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_organization"))
    private Organization organization;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 20)
    private UnitOfMeasure unit = UnitOfMeasure.UNIT;

    /** Costo de reposicion. Cuatro decimales: los costos unitarios se prorratean. */
    @Column(name = "cost", precision = 19, scale = 4)
    private BigDecimal cost;

    /** Precio de venta al publico. */
    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;

    /** {@code false} para servicios y consumibles que no se cuentan. */
    @Builder.Default
    @Column(name = "track_stock", nullable = false)
    private boolean trackStock = true;

    @Column(name = "min_stock", precision = 19, scale = 4)
    private BigDecimal minStock;

    @Column(name = "max_stock", precision = 19, scale = 4)
    private BigDecimal maxStock;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Codigos comerciales del producto. {@code orphanRemoval} es lo que convierte el
     * quitar un codigo de la coleccion en un DELETE de la fila.
     *
     * <p>Es un {@code Set} y no un {@code List} por la regla de la libreria: las
     * colecciones se modelan como {@code Set} para poder hacer varios {@code join
     * fetch} en una misma consulta sin toparse con {@code MultipleBagFetchException}.
     * El producto trae a la vez sus codigos y sus suplidores.</p>
     */
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = ProductCode.FIELD_PRODUCT, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductCode> codes = new LinkedHashSet<>();

    /** Lado propietario de la N:M con {@link Supplier}. Ver {@link ProductSupplier}. */
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = ProductSupplier.FIELD_PRODUCT,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<ProductSupplier> productSuppliers = new LinkedHashSet<>();

    /**
     * Codigo interno de la compania, derivado del codigo de tipo SKU. Es la columna
     * que muestran los listados; la gestion de codigos vive en el servicio de
     * productos.
     */
    public String skuValue() {
        return this.codes.stream()
                .filter(code -> code.getType() == ProductCodeType.SKU)
                .map(ProductCode::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Product{" +
               "name='" + name + '\'' +
               ", status=" + status +
               "} " + super.toString();
    }
}
