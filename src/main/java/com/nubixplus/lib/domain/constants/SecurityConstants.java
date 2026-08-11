package com.nubixplus.lib.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Constantes compartidas del esquema de seguridad (claims del JWT, prefijos, etc).
 */
@UtilityClass
public class SecurityConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "Bearer";
    public static final String ROLE_PREFIX = "ROLE_";

    /** Claims del JWT. */
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FULL_NAME = "name";
    public static final String CLAIM_ORGANIZATION_ID = "orgId";
    public static final String CLAIM_ORGANIZATION_DOCUMENT = "orgDoc";
    public static final String CLAIM_ORGANIZATION_NAME = "orgName";
    public static final String CLAIM_MEMBERSHIP_ID = "memberId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "perms";
    public static final String CLAIM_TOKEN_TYPE = "typ";

    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
}
