package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.Brand;
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

/** Marca del catalogo. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandDTO extends BaseDTO {

    private String code;
    private String name;
    private String description;
    private boolean active;

    protected BrandDTO(Brand brand) {
        super(brand);
        if (Objects.nonNull(brand)) {
            this.code = brand.getCode();
            this.name = brand.getName();
            this.description = brand.getDescription();
            this.active = brand.isActive();
        }
    }

    public static BrandDTO of(Brand brand) {
        return Optional.ofNullable(brand).map(BrandDTO::new).orElse(null);
    }

    public static List<BrandDTO> of(Collection<Brand> brands) {
        if (Objects.isNull(brands)) {
            return List.of();
        }
        return brands.stream()
                .sorted(Comparator.comparing(Brand::getName))
                .map(BrandDTO::of)
                .toList();
    }
}