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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Column(name = "brand", length = 120)
    private String brand;

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

    /* ---------------------------- Codigos (1:N) ---------------------------- */

    /** Codigo del tipo indicado, si el producto tiene alguno. */
    public Optional<ProductCode> findCode(ProductCodeType type) {
        return this.codes.stream().filter(code -> code.getType() == type).findFirst();
    }

    /** Valor del codigo del tipo indicado, o {@code null} si el producto no lo tiene. */
    public String codeValue(ProductCodeType type) {
        return this.findCode(type).map(ProductCode::getValue).orElse(null);
    }

    /** Codigo interno de la compania. Atajo de {@code codeValue(SKU)} para listados. */
    public String skuValue() {
        return this.codeValue(ProductCodeType.SKU);
    }

    public boolean hasCode(ProductCodeType type, String value) {
        return this.codes.stream().anyMatch(code -> code.matches(type, value));
    }

    /**
     * Agrega un codigo. Es idempotente: si el producto ya tiene ese tipo con ese
     * valor no lo duplica, que es ademas lo que garantiza
     * {@code uk_product_codes_organization_type_value} en la base de datos.
     */
    public ProductCode addCode(ProductCodeType type, String value) {
        if (Objects.isNull(type) || Objects.isNull(value)) {
            return null;
        }
        return this.codes.stream()
                .filter(code -> code.matches(type, value))
                .findFirst()
                .orElseGet(() -> {
                    final ProductCode code = ProductCode.of(this, type, value);
                    this.codes.add(code);
                    return code;
                });
    }

    public void removeCode(ProductCodeType type, String value) {
        this.codes.removeIf(code -> code.matches(type, value));
    }

    /* --------------------------- Suplidores (N:M) --------------------------- */

    /** Suplidores del producto, resueltos desde la tabla intermedia. */
    public Set<Supplier> getSuppliers() {
        return this.productSuppliers.stream()
                .map(ProductSupplier::getSupplier)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Reemplaza los suplidores sincronizando la tabla intermedia: quita los vinculos
     * que sobran y agrega los que faltan, sin recrear los que ya existian (asi no se
     * pierden el costo ni el tiempo de entrega ya pactados).
     */
    public void setSuppliers(Collection<Supplier> suppliers) {
        final Set<Supplier> target = Objects.isNull(suppliers)
                ? Set.of()
                : suppliers.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        this.productSuppliers.removeIf(link -> target.stream().noneMatch(link::linksSupplier));
        target.stream().filter(supplier -> !this.hasSupplier(supplier)).forEach(this::addSupplier);
    }

    /**
     * Vincula un suplidor. Es idempotente: si el vinculo ya existe no lo duplica, que
     * es lo que ademas garantiza {@code uk_product_suppliers} en la base de datos.
     */
    public ProductSupplier addSupplier(Supplier supplier) {
        if (Objects.isNull(supplier)) {
            return null;
        }
        return this.findLink(supplier).orElseGet(() -> {
            final ProductSupplier link = ProductSupplier.of(this, supplier);
            this.productSuppliers.add(link);
            return link;
        });
    }

    public void removeSupplier(Supplier supplier) {
        this.productSuppliers.removeIf(link -> link.linksSupplier(supplier));
    }

    public boolean hasSupplier(Supplier supplier) {
        return this.findLink(supplier).isPresent();
    }

    /** Vinculo con un suplidor concreto, para leer o ajustar sus condiciones. */
    public Optional<ProductSupplier> findLink(Supplier supplier) {
        return this.productSuppliers.stream().filter(link -> link.linksSupplier(supplier)).findFirst();
    }

    /** Suplidor marcado como preferido, si hay alguno. */
    public Optional<Supplier> getPreferredSupplier() {
        return this.productSuppliers.stream()
                .filter(ProductSupplier::isPreferred)
                .map(ProductSupplier::getSupplier)
                .findFirst();
    }

    /** Familia a la que pertenece, derivada de la categoria. */
    public Family getFamily() {
        return Optional.ofNullable(this.category).map(Category::getFamily).orElse(null);
    }

    public boolean isAvailable() {
        return Objects.nonNull(status) && status.isAvailable();
    }

    @Override
    public String toString() {
        return "Product{" +
               "name='" + name + '\'' +
               ", status=" + status +
               "} " + super.toString();
    }
}
