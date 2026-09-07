package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.Brand;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Marcas del catalogo. Toda consulta va atada a la compania: el catalogo es de cada
 * tenant, no de la plataforma.
 */
public interface BrandRepository extends BaseRepository<Brand> {

    /** Lectura por id acotada al tenant: sin esto se lee la marca de otra compania. */
    Optional<Brand> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Brand> findByCodeAndOrganizationId(String code, Long organizationId);

    List<Brand> findAllByOrganizationIdOrderByNameAsc(Long organizationId);

    boolean existsByCodeAndOrganizationId(String code, Long organizationId);

    default Optional<Brand> findByCode(String code, Long organizationId) {
        return findByCodeAndOrganizationId(code, organizationId);
    }
}