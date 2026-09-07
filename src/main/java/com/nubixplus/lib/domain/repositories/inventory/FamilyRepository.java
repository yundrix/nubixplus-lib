package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.Family;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Familias del catalogo. Toda consulta va atada a la compania: el catalogo es de
 * cada tenant, no de la plataforma.
 */
public interface FamilyRepository extends BaseRepository<Family> {

    /** Lectura por id acotada al tenant: sin esto se lee la familia de otra compania. */
    Optional<Family> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Family> findByCodeAndOrganizationId(String code, Long organizationId);

    List<Family> findAllByOrganizationIdOrderByCodeAsc(Long organizationId);

    boolean existsByCodeAndOrganizationId(String code, Long organizationId);

    default Optional<Family> findByCode(String code, Long organizationId) {
        return findByCodeAndOrganizationId(code, organizationId);
    }
}
