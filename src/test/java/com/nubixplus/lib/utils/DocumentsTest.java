package com.nubixplus.lib.utils;

import com.nubixplus.lib.domain.types.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentsTest {

    @Test
    @DisplayName("normalize deja solo digitos y devuelve null si no queda nada")
    void normalize() {
        assertThat(Documents.normalize("131-24567-8")).isEqualTo("131245678");
        assertThat(Documents.normalize("131 245 678")).isEqualTo("131245678");
        assertThat(Documents.normalize("---")).isNull();
        assertThat(Documents.normalize(null)).isNull();
    }

    @Test
    @DisplayName("isValid solo acepta 9 (RNC) u 11 (cedula) digitos")
    void isValid() {
        assertThat(Documents.isValid("131-24567-8")).isTrue();
        assertThat(Documents.isValid("001-1234567-8")).isTrue();
        assertThat(Documents.isValid("12345")).isFalse();
    }

    @Test
    @DisplayName("detectType deduce el tipo por la cantidad de digitos")
    void detectType() {
        assertThat(Documents.detectType("131245678")).isEqualTo(DocumentType.RNC);
        assertThat(Documents.detectType("00112345678")).isEqualTo(DocumentType.CEDULA);
        assertThat(Documents.detectType("123")).isNull();
    }

    @Test
    @DisplayName("format agrupa segun el tipo de documento")
    void format() {
        assertThat(Documents.format("131245678")).isEqualTo("131-24567-8");
        assertThat(Documents.format("00112345678")).isEqualTo("001-1234567-8");
    }

    @Test
    @DisplayName("matches compara sin importar el formato")
    void matches() {
        assertThat(Documents.matches("131-24567-8", "131245678")).isTrue();
        assertThat(Documents.matches("131245678", "001123456")).isFalse();
        assertThat(Documents.matches(null, "131245678")).isFalse();
    }
}
