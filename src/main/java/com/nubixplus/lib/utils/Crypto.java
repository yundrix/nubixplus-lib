package com.nubixplus.lib.utils;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@UtilityClass
public class Crypto {

    public static final String HmacSHA256 = "HmacSHA256";
    public static final String HmacSHA384 = "HmacSHA384";
    public static final String HmacSHA512 = "HmacSHA512";

    private static final String SHA_256 = "SHA-256";

    /**
     * Hash SHA-256 en hexadecimal. Se usa para guardar el refresh token: en la tabla
     * nunca vive el token en claro, asi que filtrar la base de datos no permite
     * reutilizarlo.
     */
    public String sha256(String value) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }
}
