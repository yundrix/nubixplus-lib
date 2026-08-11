package com.nubixplus.lib.domain.types;

/**
 * Tipo de documento de identidad de una compania en Republica Dominicana.
 */
public enum DocumentType {

    /** Registro Nacional del Contribuyente: 9 digitos. */
    RNC(9),

    /** Cedula de identidad y electoral: 11 digitos. */
    CEDULA(11);

    private final int length;

    DocumentType(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    /** Deduce el tipo a partir de la cantidad de digitos del documento ya normalizado. */
    public static DocumentType fromDigits(String digits) {
        if (digits == null) {
            return null;
        }
        return switch (digits.length()) {
            case 9 -> RNC;
            case 11 -> CEDULA;
            default -> null;
        };
    }
}
