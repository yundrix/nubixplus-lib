package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.ProductSupplier;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Tabla intermedia {@code product_suppliers}. Es una entidad de primera clase: la
 * relacion N:M entre producto y suplidor no vive escondida en el ORM.
 */
public interface ProductSupplierRepository extends BaseRepository<ProductSupplier> {

    @EntityGraph(attributePaths = "supplier")
    List<ProductSupplier> findAllByProductId(Long productId);

    @EntityGraph(attributePaths = "product")
    List<ProductSupplier> findAllBySupplierId(Long supplierId);

    Optional<ProductSupplier> findByProductIdAndSupplierId(Long productId, Long supplierId);

    boolean existsByProductIdAndSupplierId(Long productId, Long supplierId);

    long countByProductId(Long productId);

    long countBySupplierId(Long supplierId);

    void deleteByProductIdAndSupplierId(Long productId, Long supplierId);
}
