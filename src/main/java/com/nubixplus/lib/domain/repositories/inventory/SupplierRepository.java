package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.Supplier;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Suplidores de la compania. El documento llega ya normalizado (solo digitos), igual
 * que en {@code OrganizationRepository}.
 */
public interface SupplierRepository extends BaseRepository<Supplier> {

    /** Lectura por id acotada al tenant: sin esto se lee el suplidor de otra compania. */
    Optional<Supplier> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Supplier> findByCodeAndOrganizationId(String code, Long organizationId);

    Optional<Supplier> findByDocumentNumberAndOrganizationId(String documentNumber, Long organizationId);

    List<Supplier> findAllByOrganizationIdOrderByCodeAsc(Long organizationId);

    List<Supplier> findAllByIdInAndOrganizationId(Set<Long> ids, Long organizationId);

    boolean existsByCodeAndOrganizationId(String code, Long organizationId);

    boolean existsByDocumentNumberAndOrganizationId(String documentNumber, Long organizationId);

    default Optional<Supplier> findByDocument(String documentNumber, Long organizationId) {
        return findByDocumentNumberAndOrganizationId(documentNumber, organizationId);
    }
}
