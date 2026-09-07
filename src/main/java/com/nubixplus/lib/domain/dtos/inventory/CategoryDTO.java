package com.nubixplus.lib.domain.dtos.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.inventory.Category;
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

/** Categoria con la familia a la que pertenece. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO extends BaseDTO {

    private String code;
    private String name;
    private String description;
    private boolean active;
    private Long familyId;
    private String familyCode;
    private String familyName;

    protected CategoryDTO(Category category) {
        super(category);
        if (Objects.nonNull(category)) {
            this.code = category.getCode();
            this.name = category.getName();
            this.description = category.getDescription();
            this.active = category.isActive();

            final Family family = category.getFamily();
            if (Objects.nonNull(family)) {
                this.familyId = family.getId();
                this.familyCode = family.getCode();
                this.familyName = family.getName();
            }
        }
    }

    public static CategoryDTO of(Category category) {
        return Optional.ofNullable(category).map(CategoryDTO::new).orElse(null);
    }

    public static List<CategoryDTO> of(Collection<Category> categories) {
        if (Objects.isNull(categories)) {
            return List.of();
        }
        return categories.stream()
                .sorted(Comparator.comparing(Category::getCode))
                .map(CategoryDTO::of)
                .toList();
    }
}
