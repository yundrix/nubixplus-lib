package com.nubixplus.lib.domain.types;

/**
 * Estado comercial de un producto.
 */
public enum ProductStatus {
    /** Se puede comprar y vender. */
    ACTIVE,
    /** Fuera de circulacion temporalmente. */
    INACTIVE,
    /** Descontinuado: no se vuelve a comprar, solo se agota la existencia. */
    DISCONTINUED;

    public boolean isAvailable() {
        return this == ACTIVE;
    }
}
