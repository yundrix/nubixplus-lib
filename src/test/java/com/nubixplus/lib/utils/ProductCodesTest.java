package com.nubixplus.lib.utils;

import com.nubixplus.lib.domain.types.ProductCodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCodesTest {

    /** Codigos reales, con digito verificador correcto. */
    private static final String UPC_12 = "036000291452";
    private static final String EAN_13 = "4006381333931";
    private static final String EAN_8 = "96385074";

    @Test
    @DisplayName("normalizeBarcode deja solo digitos y devuelve null si no queda nada")
    void normalizeBarcode() {
        assertThat(ProductCodes.normalizeBarcode("0 36000 29145 2")).isEqualTo(UPC_12);
        assertThat(ProductCodes.normalizeBarcode("4-006381-333931")).isEqualTo(EAN_13);
        assertThat(ProductCodes.normalizeBarcode("--")).isNull();
        assertThat(ProductCodes.normalizeBarcode(null)).isNull();
    }

    @Test
    @DisplayName("normalizeFree deja MAYUSCULAS sin espacios ni simbolos raros")
    void normalizeFree() {
        assertThat(ProductCodes.normalizeFree(" cafe-molido_500 ")).isEqualTo("CAFE-MOLIDO_500");
        assertThat(ProductCodes.normalizeFree("sku 001")).isEqualTo("SKU001");
        assertThat(ProductCodes.normalizeFree("###")).isNull();
        assertThat(ProductCodes.normalizeFree(null)).isNull();
    }

    @Test
    @DisplayName("normalize elige la regla segun el tipo del codigo")
    void normalizeByType() {
        assertThat(ProductCodes.normalize(ProductCodeType.UPC12, "0 36000 29145 2")).isEqualTo(UPC_12);
        assertThat(ProductCodes.normalize(ProductCodeType.SKU, "cafe-001")).isEqualTo("CAFE-001");
        assertThat(ProductCodes.normalize(ProductCodeType.CUSTOM, "lote/2026")).isEqualTo("LOTE/2026");
        assertThat(ProductCodes.normalize(null, "algo")).isNull();
    }

    @Test
    @DisplayName("Los GTIN exigen el largo del estandar y el digito verificador")
    void validatesBarcodes() {
        assertThat(ProductCodes.isValid(ProductCodeType.UPC12, UPC_12)).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.UPC12, "0 36000 29145 2")).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.EAN13, EAN_13)).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.EAN8, EAN_8)).isTrue();

        // Mismo codigo con el verificador cambiado.
        assertThat(ProductCodes.isValid(ProductCodeType.UPC12, "036000291453")).isFalse();
        assertThat(ProductCodes.isValid(ProductCodeType.EAN13, "4006381333932")).isFalse();

        // Largo que no corresponde al tipo.
        assertThat(ProductCodes.isValid(ProductCodeType.EAN13, UPC_12)).isFalse();
        assertThat(ProductCodes.isValid(ProductCodeType.UPC12, EAN_13)).isFalse();
        assertThat(ProductCodes.isValid(ProductCodeType.EAN8, null)).isFalse();
    }

    @Test
    @DisplayName("EAN14 valida como cualquier otro GTIN")
    void validatesEan14() {
        // GTIN-14: 13 digitos mas el verificador que calcula la propia utilidad.
        final String payload = "1400638133393";
        final String ean14 = payload + ProductCodes.checkDigit(payload);

        assertThat(ean14).hasSize(14);
        assertThat(ProductCodes.isValid(ProductCodeType.EAN14, ean14)).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.EAN14, payload + "0")).isFalse();
    }

    @Test
    @DisplayName("UPC6 exige seis digitos y no lleva verificador propio")
    void validatesUpc6() {
        assertThat(ProductCodes.isValid(ProductCodeType.UPC6, "123456")).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.UPC6, "12345")).isFalse();
        assertThat(ProductCodes.isValid(ProductCodeType.UPC6, "1234567")).isFalse();
    }

    @Test
    @DisplayName("SKU y CUSTOM aceptan el formato del negocio, no solo digitos")
    void validatesFreeCodes() {
        assertThat(ProductCodes.isValid(ProductCodeType.SKU, "CAFE-001")).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.SKU, "cafe.molido/500")).isTrue();
        assertThat(ProductCodes.isValid(ProductCodeType.CUSTOM, "LOTE-2026-A")).isTrue();

        assertThat(ProductCodes.isValid(ProductCodeType.SKU, "   ")).isFalse();
        assertThat(ProductCodes.isValid(ProductCodeType.CUSTOM, "X".repeat(ProductCodes.MAX_LENGTH + 1))).isFalse();
    }

    @Test
    @DisplayName("checkDigit calcula el digito de control de un GTIN")
    void checkDigit() {
        assertThat(ProductCodes.checkDigit("03600029145")).isEqualTo(2);
        assertThat(ProductCodes.checkDigit("400638133393")).isEqualTo(1);
        assertThat(ProductCodes.checkDigit("9638507")).isEqualTo(4);
    }

    @Test
    @DisplayName("format separa el digito verificador")
    void format() {
        assertThat(ProductCodes.format(UPC_12)).isEqualTo("03600029145-2");
        assertThat(ProductCodes.format(null)).isNull();
    }

    @Test
    @DisplayName("ProductCodeType.from resuelve el tipo sin distinguir mayusculas")
    void resolvesType() {
        assertThat(ProductCodeType.from("ean13")).isEqualTo(ProductCodeType.EAN13);
        assertThat(ProductCodeType.from(" SKU ")).isEqualTo(ProductCodeType.SKU);
        assertThat(ProductCodeType.from("no-existe")).isNull();
        assertThat(ProductCodeType.from(null)).isNull();
    }
}
