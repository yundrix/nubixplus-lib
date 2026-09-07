package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.Family;
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

/** Familia del catalogo: el nivel superior de la clasificacion. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FamilyDTO extends BaseDTO {

    private String code;
    private String name;
    private String description;
    private boolean active;

    protected FamilyDTO(Family family) {
        super(family);
        if (Objects.nonNull(family)) {
            this.code = family.getCode();
            this.name = family.getName();
            this.description = family.getDescription();
            this.active = family.isActive();
        }
    }

    public static FamilyDTO of(Family family) {
        return Optional.ofNullable(family).map(FamilyDTO::new).orElse(null);
    }

    /** Ordenadas por codigo para que el front siempre reciba la misma secuencia. */
    public static List<FamilyDTO> of(Collection<Family> families) {
        if (Objects.isNull(families)) {
            return List.of();
        }
        return families.stream()
                .sorted(Comparator.comparing(Family::getCode))
                .map(FamilyDTO::of)
                .toList();
    }
}
