package com.nubixplus.lib.utils;

import com.nubixplus.lib.domain.types.ProductCodeType;
import lombok.experimental.UtilityClass;

import java.util.Objects;

/**
 * Normalizacion y validacion de los codigos comerciales de un producto.
 *
 * <p>Mismo criterio que {@link Documents}: el codigo se guarda y se compara SIEMPRE
 * normalizado, de modo que {@code 0 12345 67890 5} y {@code 012345678905} sean el
 * mismo valor. Los codigos se manejan como texto y nunca como numeros: un EAN puede
 * empezar en cero y ese cero es parte del codigo.</p>
 *
 * <p>Es el unico lugar con las reglas por tipo:
 * {@link ProductCodeType} declara el formato y esta clase lo aplica.</p>
 *
 * <ul>
 *   <li><b>GTIN</b> (EAN-8, EAN-13, EAN-14, UPC-A): suma ponderada 3-1 de derecha a
 *       izquierda; el total mas el digito verificador debe ser multiplo de 10.</li>
 *   <li><b>UPC-E</b> ({@code UPC6}): seis digitos comprimidos, sin verificador propio.</li>
 *   <li><b>SKU</b> y <b>CUSTOM</b>: los define el negocio. Solo se exige que no queden
 *       vacios y que quepan en la columna.</li>
 * </ul>
 */
@UtilityClass
public class ProductCodes {

    /** Largo maximo de la columna {@code product_codes.code_value}. */
    public static final int MAX_LENGTH = 60;

    private static final String NON_DIGITS = "\\D";
    private static final String FREE_INVALID_CHARS = "[^A-Z0-9._/\\-]";
    private static final int CHECK_DIGIT_MODULUS = 10;
    private static final int ODD_WEIGHT = 3;

    /** Deja solo digitos. Devuelve {@code null} si la entrada es nula o queda vacia. */
    public String normalizeBarcode(String code) {
        if (Objects.isNull(code)) {
            return null;
        }
        final String digits = code.replaceAll(NON_DIGITS, "");
        return digits.isEmpty() ? null : digits;
    }

    /**
     * MAYUSCULAS sin espacios ni simbolos raros. Se usa para los codigos de largo
     * libre (SKU y CUSTOM), que no son necesariamente numericos.
     */
    public String normalizeFree(String code) {
        if (Objects.isNull(code)) {
            return null;
        }
        final String normalized = code.trim().toUpperCase().replaceAll(FREE_INVALID_CHARS, "");
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normaliza segun el tipo: los codigos de barra quedan en solo digitos y los de
     * largo libre en MAYUSCULAS.
     *
     * @return el codigo listo para guardar, o {@code null} si no queda nada util
     */
    public String normalize(ProductCodeType type, String code) {
        if (Objects.isNull(type)) {
            return null;
        }
        return type.isBarcode() ? normalizeBarcode(code) : normalizeFree(code);
    }

    /**
     * Valida un codigo contra las reglas de su tipo: largo exacto cuando el estandar
     * lo fija y digito verificador cuando el estandar lo tiene.
     *
     * @param type tipo del codigo
     * @param code valor tal como lo envio el cliente; se normaliza antes de validar
     * @return {@code true} si el codigo es utilizable para ese tipo
     */
    public boolean isValid(ProductCodeType type, String code) {
        final String normalized = normalize(type, code);
        if (Objects.isNull(normalized) || normalized.length() > MAX_LENGTH) {
            return false;
        }
        if (!type.isFreeLength() && normalized.length() != type.getLength()) {
            return false;
        }
        return !type.hasCheckDigit() || hasValidCheckDigit(normalized);
    }

    /**
     * Verifica el digito de control de un GTIN (UPC-A, EAN-8, EAN-13, EAN-14).
     *
     * @param digits codigo ya normalizado, solo digitos
     * @return {@code true} si el ultimo digito coincide con el calculado
     */
    public boolean hasValidCheckDigit(String digits) {
        if (Objects.isNull(digits) || digits.length() < 2) {
            return false;
        }
        final String payload = digits.substring(0, digits.length() - 1);
        final int declared = Character.getNumericValue(digits.charAt(digits.length() - 1));
        return declared == checkDigit(payload);
    }

    /**
     * Calcula el digito verificador de un GTIN a partir del codigo SIN el.
     *
     * @param payload codigo sin el digito de control (7, 11, 12 o 13 digitos)
     * @return el digito verificador, de 0 a 9
     */
    public int checkDigit(String payload) {
        int sum = 0;
        int weight = ODD_WEIGHT;
        for (int index = payload.length() - 1; index >= 0; index--) {
            sum += Character.getNumericValue(payload.charAt(index)) * weight;
            weight = weight == ODD_WEIGHT ? 1 : ODD_WEIGHT;
        }
        return (CHECK_DIGIT_MODULUS - (sum % CHECK_DIGIT_MODULUS)) % CHECK_DIGIT_MODULUS;
    }

    /** Formatea un GTIN para mostrar, separando el digito verificador: {@code 01234567890-5}. */
    public String format(String code) {
        final String digits = normalizeBarcode(code);
        if (Objects.isNull(digits) || digits.length() < 2) {
            return digits;
        }
        return digits.substring(0, digits.length() - 1) + "-" + digits.charAt(digits.length() - 1);
    }
}
