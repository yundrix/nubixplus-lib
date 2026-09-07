package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.inventory.Category;
import com.nubixplus.lib.domain.entities.inventory.Family;
import com.nubixplus.lib.domain.entities.inventory.Product;
import com.nubixplus.lib.domain.types.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Producto con su clasificacion y sus valores comerciales. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO extends BaseProductDTO {

    private String description;
    private String brand;
    private UnitOfMeasure unit;
    private String unitSymbol;
    private BigDecimal cost;
    private BigDecimal price;
    private boolean trackStock;
    private BigDecimal minStock;
    private BigDecimal maxStock;

    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Long familyId;
    private String familyCode;
    private String familyName;

    protected ProductDTO(Product product) {
        super(product);
        this.description = product.getDescription();
        this.brand = product.getBrand();
        this.unit = product.getUnit();
        this.unitSymbol = Optional.ofNullable(product.getUnit()).map(UnitOfMeasure::getSymbol).orElse(null);
        this.cost = product.getCost();
        this.price = product.getPrice();
        this.trackStock = product.isTrackStock();
        this.minStock = product.getMinStock();
        this.maxStock = product.getMaxStock();

        final Category category = product.getCategory();
        if (Objects.nonNull(category)) {
            this.categoryId = category.getId();
            this.categoryCode = category.getCode();
            this.categoryName = category.getName();

            final Family family = category.getFamily();
            if (Objects.nonNull(family)) {
                this.familyId = family.getId();
                this.familyCode = family.getCode();
                this.familyName = family.getName();
            }
        }
    }

    public static ProductDTO of(Product product) {
        return Optional.ofNullable(product).map(ProductDTO::new).orElse(null);
    }

    public static List<ProductDTO> of(Collection<Product> products) {
        if (Objects.isNull(products)) {
            return List.of();
        }
        return products.stream()
                .sorted(Comparator.comparing(Product::getName))
                .map(ProductDTO::of)
                .toList();
    }
}
