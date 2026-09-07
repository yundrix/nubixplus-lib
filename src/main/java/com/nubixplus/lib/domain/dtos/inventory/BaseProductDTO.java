package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.Product;
import com.nubixplus.lib.domain.types.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Identidad del producto: su nombre y sus codigos. Es lo que se embebe donde no hace
 * falta la ficha completa.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseProductDTO extends BaseDTO {

    private String name;
    private ProductStatus status;

    /** Todos los codigos del producto, tal como estan en {@code product_codes}. */
    private List<ProductCodeDTO> codes;

    /**
     * Codigo interno, derivado del codigo de tipo SKU. No es un atributo del producto:
     * se expone aparte porque es la columna que muestran los listados.
     */
    private String sku;

    public BaseProductDTO(Product product) {
        super(product);
        if (Objects.nonNull(product)) {
            this.name = product.getName();
            this.status = product.getStatus();
            this.codes = ProductCodeDTO.of(product.getCodes());
            this.sku = product.skuValue();
        }
    }

    public static BaseProductDTO of(Product product) {
        return Optional.ofNullable(product).map(BaseProductDTO::new).orElse(null);
    }
}
