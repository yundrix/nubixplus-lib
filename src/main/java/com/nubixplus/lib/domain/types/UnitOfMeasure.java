package com.nubixplus.lib.domain.types;

import java.util.Arrays;
import java.util.Objects;

/**
 * Unidad en la que se mide y se vende un producto.
 *
 * <p>El {@code symbol} es lo que se muestra en documentos (facturas, ordenes de
 * compra); el nombre del enum es lo que se persiste.</p>
 */
public enum UnitOfMeasure {

    UNIT("und"),
    PACK("paq"),
    BOX("caj"),
    DOZEN("doc"),
    GRAM("g"),
    KILOGRAM("kg"),
    POUND("lb"),
    MILLILITER("ml"),
    LITER("L"),
    GALLON("gal"),
    METER("m"),
    FOOT("ft");

    /** Simbolo para documentos (facturas, ordenes de compra). */
    private final String symbol;

    UnitOfMeasure(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /** Resuelve la unidad por su nombre o por su simbolo, sin distinguir mayusculas. */
    public static UnitOfMeasure from(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }
        final String normalized = value.trim();
        return Arrays.stream(values())
                .filter(unit -> unit.name().equalsIgnoreCase(normalized) || unit.symbol.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}
