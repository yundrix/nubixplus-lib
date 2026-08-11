package com.nubixplus.lib.stereotype;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entidad con trazabilidad de autor, control de concurrencia optimista y borrado
 * logico. Las fechas las sigue manejando {@link BaseEntity}.
 *
 * <p>Requiere que el servicio active {@code @EnableJpaAuditing} y publique un
 * {@code AuditorAware<String>} (ver {@code JpaConfig} en nubixplus-services).</p>
 */
@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    public static final String FIELD_DELETED = "deleted";

    @CreatedBy
    @Column(name = "created_by", length = 120, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 120)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /** Borrado logico: las consultas de negocio filtran siempre por {@code deleted = false}. */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    public void markDeleted() {
        this.deleted = Boolean.TRUE;
    }
}
