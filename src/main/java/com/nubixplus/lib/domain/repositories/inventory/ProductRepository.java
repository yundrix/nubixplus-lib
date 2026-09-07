package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.Product;
import com.nubixplus.lib.domain.types.ProductCodeType;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Productos del catalogo.
 *
 * <p>Los servicios corren con {@code open-in-view: false}: el grafo que van a leer
 * los DTO tiene que venir resuelto desde la consulta, por eso los {@code EntityGraph}
 * sobre marca, categoria y familia.</p>
 */
public interface ProductRepository extends BaseRepository<Product> {

    @Override
    @EntityGraph(attributePaths = {"brand", "category", "category.family", "codes"})
    Page<Product> findAll(Specification<Product> specification, Pageable pageable);

    /** Lectura por id acotada al tenant; el grafo completo viene en la misma consulta. */
    @EntityGraph(attributePaths = {"brand", "category", "category.family", "codes",
            "productSuppliers", "productSuppliers.supplier"})
    Optional<Product> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Busqueda por codigo: es la consulta del lector de codigo de barras y la que
     * usa el alta para no repetir un identificador.
     */
    @EntityGraph(attributePaths = {"brand", "category", "category.family", "codes",
            "productSuppliers", "productSuppliers.supplier"})
    @Query("""
            select p from Product p
              join p.codes c
             where c.type = :type
               and c.value = :value
               and p.organization.id = :organizationId
            """)
    Optional<Product> findByCode(@Param("type") ProductCodeType type,
                                 @Param("value") String value,
                                 @Param("organizationId") Long organizationId);

    /** Impide desactivar o borrar una categoria que todavia clasifica productos. */
    boolean existsByCategoryId(Long categoryId);

    /** Impide borrar una marca que todavia respalda productos. */
    boolean existsByBrandId(Long brandId);

    default Optional<Product> findBySku(String sku, Long organizationId) {
        return findByCode(ProductCodeType.SKU, sku, organizationId);
    }
}
