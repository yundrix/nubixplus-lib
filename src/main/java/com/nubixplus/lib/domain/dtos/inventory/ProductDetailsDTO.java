package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.inventory.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Ficha completa del producto: agrega los suplidores que lo abastecen, leidos desde
 * la tabla intermedia {@code product_suppliers}.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailsDTO extends ProductDTO {

    private List<ProductSupplierDTO> suppliers;

    protected ProductDetailsDTO(Product product) {
        super(product);
        this.suppliers = ProductSupplierDTO.of(product.getProductSuppliers());
    }

    public static ProductDetailsDTO of(Product product) {
        return Optional.ofNullable(product).map(ProductDetailsDTO::new).orElse(null);
    }
}
