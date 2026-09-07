package com.nubixplus.lib.domain.repositories.inventory;

import com.nubixplus.lib.domain.entities.inventory.ProductCode;
import com.nubixplus.lib.domain.types.ProductCodeType;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Codigos comerciales de los productos. La busqueda por valor es la del lector de
 * codigo de barras, por eso va siempre atada a la compania.
 */
public interface ProductCodeRepository extends BaseRepository<ProductCode> {

    List<ProductCode> findAllByProductId(Long productId);

    Optional<ProductCode> findByOrganizationIdAndTypeAndValue(Long organizationId,
                                                              ProductCodeType type,
                                                              String value);

    /** Un mismo valor puede existir bajo tipos distintos: por eso devuelve lista. */
    List<ProductCode> findAllByOrganizationIdAndValue(Long organizationId, String value);

    boolean existsByOrganizationIdAndTypeAndValue(Long organizationId, ProductCodeType type, String value);

    long countByProductId(Long productId);

    void deleteByProductIdAndType(Long productId, ProductCodeType type);
}
