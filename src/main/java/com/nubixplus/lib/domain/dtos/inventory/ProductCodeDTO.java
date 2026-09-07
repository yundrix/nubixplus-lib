package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.ProductCode;
import com.nubixplus.lib.domain.types.ProductCodeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Un codigo comercial del producto: su tipo y su valor. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCodeDTO extends BaseDTO {

    private ProductCodeType type;
    private String value;

    protected ProductCodeDTO(ProductCode code) {
        super(code);
        if (Objects.nonNull(code)) {
            this.type = code.getType();
            this.value = code.getValue();
        }
    }

    public static ProductCodeDTO of(ProductCode code) {
        return Optional.ofNullable(code).map(ProductCodeDTO::new).orElse(null);
    }

    /** El SKU primero y el resto por tipo: el front siempre recibe la misma secuencia. */
    public static List<ProductCodeDTO> of(Collection<ProductCode> codes) {
        if (Objects.isNull(codes)) {
            return List.of();
        }
        return codes.stream()
                .map(ProductCodeDTO::of)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((ProductCodeDTO dto) -> dto.getType() != ProductCodeType.SKU)
                        .thenComparing(dto -> Objects.requireNonNullElse(dto.getType(), ProductCodeType.CUSTOM))
                        .thenComparing(dto -> Objects.requireNonNullElse(dto.getValue(), "")))
                .toList();
    }
}
