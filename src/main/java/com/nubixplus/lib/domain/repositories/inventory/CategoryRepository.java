package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.Category;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Categorias del catalogo, siempre dentro de una familia y de una compania.
 */
public interface CategoryRepository extends BaseRepository<Category> {

    /** Lectura por id acotada al tenant; el grafo resuelve la familia en la misma consulta. */
    @EntityGraph(attributePaths = "family")
    Optional<Category> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Category> findByCodeAndOrganizationId(String code, Long organizationId);

    @EntityGraph(attributePaths = "family")
    List<Category> findAllByOrganizationIdOrderByCodeAsc(Long organizationId);

    List<Category> findAllByFamilyIdOrderByCodeAsc(Long familyId);

    boolean existsByCodeAndOrganizationId(String code, Long organizationId);

    /** Impide desactivar o borrar una familia que todavia clasifica categorias. */
    boolean existsByFamilyId(Long familyId);

    default Optional<Category> findByCode(String code, Long organizationId) {
        return findByCodeAndOrganizationId(code, organizationId);
    }
}
