package com.nubixplus.lib.domain.repositories;

import com.nubixplus.lib.domain.entities.organization.Organization;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.Optional;

public interface OrganizationRepository extends BaseRepository<Organization> {

    /** El documento se recibe ya normalizado (solo digitos). */
    Optional<Organization> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    default Optional<Organization> findByDocument(String documentNumber) {
        return findByDocumentNumber(documentNumber);
    }

    default boolean existsByDocument(String documentNumber) {
        return existsByDocumentNumber(documentNumber);
    }
}
