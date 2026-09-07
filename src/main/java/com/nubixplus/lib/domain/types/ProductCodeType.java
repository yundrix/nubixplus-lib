package com.nubixplus.lib.domain.types;

import java.util.Arrays;
import java.util.Objects;

/**
 * Tipo de codigo comercial de un producto.
 *
 * <p>Cada valor describe SU formato; la validacion en si vive en
 * {@code ProductCodes}, que es el unico lugar donde esta el algoritmo del digito
 * verificador. Aqui solo van los metadatos, igual que {@link DocumentType} declara
 * la cantidad de digitos del RNC y la cedula sin saber normalizarlos.</p>
 *
 * <ul>
 *   <li>{@code EAN8} / {@code EAN13} / {@code EAN14}: GTIN de 8, 13 y 14 digitos.</li>
 *   <li>{@code UPC12}: UPC-A, 12 digitos.</li>
 *   <li>{@code UPC6}: UPC-E comprimido, 6 digitos, sin digito verificador propio.</li>
 *   <li>{@code SKU}: codigo interno de la compania. Lo define el negocio, no un
 *       estandar, y no tiene por que ser numerico.</li>
 *   <li>{@code CUSTOM}: cualquier otro codigo del negocio, sin formato asumido.</li>
 * </ul>
 */
public enum ProductCodeType {

    EAN8(8, true, true),
    EAN13(13, true, true),
    EAN14(14, true, true),
    UPC12(12, true, true),

    /** UPC-E: los 6 digitos comprimidos, sin verificador propio. */
    UPC6(6, true, false),

    /** Codigo interno; formato libre definido por la compania. */
    SKU(null, false, false),

    /** Codigo del negocio sin formato asumido. */
    CUSTOM(null, false, false);

    /** Cantidad exacta de caracteres, o {@code null} si el largo es libre. */
    private final Integer length;

    /** {@code true} si el codigo es exclusivamente numerico. */
    private final boolean numeric;

    /** {@code true} si el ultimo digito es el verificador del estandar GTIN. */
    private final boolean checkDigit;

    ProductCodeType(Integer length, boolean numeric, boolean checkDigit) {
        this.length = length;
        this.numeric = numeric;
        this.checkDigit = checkDigit;
    }

    public Integer getLength() {
        return length;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean hasCheckDigit() {
        return checkDigit;
    }

    /** {@code true} si el tipo es un codigo de barras estandar (GTIN o UPC). */
    public boolean isBarcode() {
        return numeric;
    }

    /** {@code true} si el largo lo define el negocio y no el estandar. */
    public boolean isFreeLength() {
        return Objects.isNull(length);
    }

    /** Resuelve el tipo por su nombre, sin distinguir mayusculas. */
    public static ProductCodeType from(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }
        final String normalized = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}
