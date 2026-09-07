package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.ProductSupplier;
import com.nubixplus.lib.domain.entities.inventory.Supplier;
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

/**
 * Una fila de la tabla intermedia {@code product_suppliers}: el suplidor y las
 * condiciones pactadas con el para ese producto.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSupplierDTO extends BaseDTO {

    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private String supplierReference;
    private BigDecimal lastCost;
    private Integer leadTimeDays;
    private boolean preferred;

    protected ProductSupplierDTO(ProductSupplier link) {
        super(link);
        if (Objects.nonNull(link)) {
            this.supplierReference = link.getSupplierReference();
            this.lastCost = link.getLastCost();
            this.leadTimeDays = link.getLeadTimeDays();
            this.preferred = link.isPreferred();

            final Supplier supplier = link.getSupplier();
            if (Objects.nonNull(supplier)) {
                this.supplierId = supplier.getId();
                this.supplierCode = supplier.getCode();
                this.supplierName = supplier.getDisplayName();
            }
        }
    }

    public static ProductSupplierDTO of(ProductSupplier link) {
        return Optional.ofNullable(link).map(ProductSupplierDTO::new).orElse(null);
    }

    /** El suplidor preferido primero; el resto por codigo. */
    public static List<ProductSupplierDTO> of(Collection<ProductSupplier> links) {
        if (Objects.isNull(links)) {
            return List.of();
        }
        return links.stream()
                .map(ProductSupplierDTO::of)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProductSupplierDTO::isPreferred).reversed()
                        .thenComparing(dto -> Objects.requireNonNullElse(dto.getSupplierCode(), "")))
                .toList();
    }
}
