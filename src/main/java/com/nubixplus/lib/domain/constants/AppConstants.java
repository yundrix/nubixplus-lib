package com.nubixplus.lib.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Constantes generales de la plataforma.
 */
@UtilityClass
public class AppConstants {

    public static final String DEFAULT_TIMEZONE = "America/Santo_Domingo";
    public static final String DEFAULT_CURRENCY = "DOP";
    public static final String DEFAULT_LOCALE = "es-DO";

    public static final String SYSTEM_USER = "system";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 200;

    /** Cantidad de intentos fallidos antes de bloquear la cuenta. */
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;

    /** Minutos que dura el bloqueo temporal de la cuenta. */
    public static final int ACCOUNT_LOCK_MINUTES = 15;
}
