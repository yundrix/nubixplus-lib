package com.nubixplus.lib.domain.entities.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * Tabla intermedia explicita entre {@link Product} y {@link Supplier}.
 *
 * <pre>
 * PRODUCT 1 ── N PRODUCT_SUPPLIER N ── 1 SUPPLIER
 * </pre>
 *
 * <p>Un producto se le puede comprar a varios suplidores y un suplidor abastece
 * varios productos, asi que la relacion es N:M. La tabla del medio no es un detalle
 * del ORM: guarda las condiciones pactadas con ese suplidor para ESE producto (su
 * referencia, el ultimo costo, el tiempo de entrega), que es informacion que no cabe
 * ni en el producto ni en el suplidor.</p>
 *
 * <p>Extiende {@link BaseEntity} como las demas tablas intermedias del proyecto: el
 * vinculo se crea o se elimina, no se borra logicamente.</p>
 */
@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(
        name = "product_suppliers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_suppliers",
                columnNames = {"product_id", "supplier_id"}),
        indexes = {
                @Index(name = "ix_product_suppliers_product", columnList = "product_id"),
                @Index(name = "ix_product_suppliers_supplier", columnList = "supplier_id")
        }
)
public class ProductSupplier extends BaseEntity {

    public static final String FIELD_PRODUCT = "product";
    public static final String FIELD_SUPPLIER = "supplier";
    public static final String FIELD_SUPPLIER_REFERENCE = "supplierReference";
    public static final String FIELD_PREFERRED = "preferred";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_suppliers_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_suppliers_supplier"))
    private Supplier supplier;

    /**
     * Codigo con el que el suplidor identifica el producto en SU catalogo. No se
     * llama {@code supplierSku} porque SKU ya nombra un {@code ProductCodeType}
     * concreto del catalogo propio: son dos cosas distintas.
     */
    @Column(name = "supplier_reference", length = 60)
    private String supplierReference;

    /** Ultimo costo de compra pactado con este suplidor. */
    @Column(name = "last_cost", precision = 19, scale = 4)
    private BigDecimal lastCost;

    /** Dias entre la orden y la entrega. */
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    /** Suplidor por defecto al reponer este producto. */
    @Column(name = "preferred", nullable = false)
    private boolean preferred;

    public static ProductSupplier of(Product product, Supplier supplier) {
        return ProductSupplier.builder().product(product).supplier(supplier).build();
    }

    /** Identidad por clave natural (los dos ids). Ver {@code RolePermission}. */
    @EqualsAndHashCode.Include
    public Long productId() {
        return Optional.ofNullable(this.product).map(Product::getId).orElse(null);
    }

    @EqualsAndHashCode.Include
    public Long supplierId() {
        return Optional.ofNullable(this.supplier).map(Supplier::getId).orElse(null);
    }

    /** Compara por id para no inicializar el proxy perezoso. */
    public boolean linksSupplier(Supplier other) {
        if (Objects.isNull(other)) {
            return false;
        }
        return Objects.nonNull(other.getId())
                ? other.getId().equals(this.supplierId())
                : other.equals(this.supplier);
    }

    public boolean linksProduct(Product other) {
        if (Objects.isNull(other)) {
            return false;
        }
        return Objects.nonNull(other.getId())
                ? other.getId().equals(this.productId())
                : other.equals(this.product);
    }

    @Override
    public String toString() {
        return "ProductSupplier{" +
               "productId=" + productId() +
               ", supplierId=" + supplierId() +
               ", preferred=" + preferred +
               "} " + super.toString();
    }
}
