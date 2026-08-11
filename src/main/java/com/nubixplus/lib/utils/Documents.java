package com.nubixplus.lib.utils;

import com.nubixplus.lib.domain.types.DocumentType;
import lombok.experimental.UtilityClass;

/**
 * Normalizacion y validacion de RNC / cedula.
 *
 * <p>El documento se guarda y compara SIEMPRE normalizado (solo digitos), de modo que
 * {@code 131-24567-8}, {@code 131 245 678} y {@code 131245678} sean el mismo valor.</p>
 */
@UtilityClass
public class Documents {

    public static final int RNC_LENGTH = 9;
    public static final int CEDULA_LENGTH = 11;

    private static final String NON_DIGITS = "\\D";
    private static final String SEPARATOR = "-";

    /** Deja solo digitos. Devuelve {@code null} si la entrada es nula o queda vacia. */
    public String normalize(String document) {
        if (document == null) {
            return null;
        }
        final String digits = document.replaceAll(NON_DIGITS, "");
        return digits.isEmpty() ? null : digits;
    }

    public boolean isValid(String document) {
        final String digits = normalize(document);
        return digits != null && (digits.length() == RNC_LENGTH || digits.length() == CEDULA_LENGTH);
    }

    public boolean isRnc(String document) {
        final String digits = normalize(document);
        return digits != null && digits.length() == RNC_LENGTH;
    }

    public boolean isCedula(String document) {
        final String digits = normalize(document);
        return digits != null && digits.length() == CEDULA_LENGTH;
    }

    /** Devuelve el tipo detectado o {@code null} si el documento no es valido. */
    public DocumentType detectType(String document) {
        return DocumentType.fromDigits(normalize(document));
    }

    /** Formatea para mostrar: RNC {@code 131-24567-8}, cedula {@code 001-1234567-8}. */
    public String format(String document) {
        final String digits = normalize(document);
        if (digits == null) {
            return null;
        }
        return switch (digits.length()) {
            case RNC_LENGTH -> digits.substring(0, 3) + SEPARATOR + digits.substring(3, 8) + SEPARATOR + digits.substring(8);
            case CEDULA_LENGTH -> digits.substring(0, 3) + SEPARATOR + digits.substring(3, 10) + SEPARATOR + digits.substring(10);
            default -> digits;
        };
    }

    /** Comparacion insensible al formato (se usa para validar el header contra el token). */
    public boolean matches(String left, String right) {
        final String normalizedLeft = normalize(left);
        final String normalizedRight = normalize(right);
        return normalizedLeft != null && normalizedLeft.equals(normalizedRight);
    }
}
