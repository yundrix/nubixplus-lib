package com.nubixplus.lib.domain.repositories;

import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.Optional;

public interface OrganizationRepository extends BaseRepository<Organization> {

    /** El documento se recibe ya normalizado (solo digitos). */
    Optional<Organization> findByDocumentNumberAndDeletedFalse(String documentNumber);

    Optional<Organization> findByIdAndDeletedFalse(Long id);

    boolean existsByDocumentNumberAndDeletedFalse(String documentNumber);

    default Optional<Organization> findByDocument(String documentNumber) {
        return findByDocumentNumberAndDeletedFalse(documentNumber);
    }

    default boolean existsByDocument(String documentNumber) {
        return existsByDocumentNumberAndDeletedFalse(documentNumber);
    }
}
