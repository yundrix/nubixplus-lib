package com.nubixplus.lib.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Codigos de los roles de sistema (los que existen en todas las organizaciones).
 */
@UtilityClass
public class RoleCodes {

    /** Acceso total a la plataforma, incluso entre organizaciones. */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** Administrador de una compania: gestiona usuarios, roles y configuracion. */
    public static final String ORG_ADMIN = "ORG_ADMIN";

    /** Usuario operativo estandar. */
    public static final String USER = "USER";

    /** Solo lectura. */
    public static final String VIEWER = "VIEWER";
}
