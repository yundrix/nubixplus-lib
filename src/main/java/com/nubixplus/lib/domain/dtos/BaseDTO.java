package com.nubixplus.lib.domain.dtos;

import com.nubixplus.lib.stereotype.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected BaseDTO(BaseEntity entity) {
        if (Objects.nonNull(entity)) {
            this.id = entity.getId();
            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();
        }
    }
}
