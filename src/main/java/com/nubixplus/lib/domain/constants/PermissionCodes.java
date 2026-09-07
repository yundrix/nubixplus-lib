package com.nubixplus.lib.domain.constants;

import lombok.experimental.UtilityClass;

/**
 * Codigos de permiso base. Se usan tanto en el seeder como en las anotaciones
 * {@code @PreAuthorize("hasAuthority('...')")} de los resources.
 */
@UtilityClass
public class PermissionCodes {

    public static final String USERS_READ = "users:read";
    public static final String USERS_CREATE = "users:create";
    public static final String USERS_UPDATE = "users:update";
    public static final String USERS_DELETE = "users:delete";

    public static final String ROLES_READ = "roles:read";
    public static final String ROLES_CREATE = "roles:create";
    public static final String ROLES_UPDATE = "roles:update";
    public static final String ROLES_DELETE = "roles:delete";

    public static final String PERMISSIONS_READ = "permissions:read";

    public static final String ORGANIZATIONS_READ = "organizations:read";
    public static final String ORGANIZATIONS_UPDATE = "organizations:update";
    public static final String ORGANIZATIONS_CREATE = "organizations:create";

    /* ----------------------------- Inventario ------------------------------ */

    public static final String PRODUCTS_READ = "products:read";
    public static final String PRODUCTS_CREATE = "products:create";
    public static final String PRODUCTS_UPDATE = "products:update";
    public static final String PRODUCTS_DELETE = "products:delete";

    public static final String CATEGORIES_READ = "categories:read";
    public static final String CATEGORIES_CREATE = "categories:create";
    public static final String CATEGORIES_UPDATE = "categories:update";
    public static final String CATEGORIES_DELETE = "categories:delete";

    public static final String FAMILIES_READ = "families:read";
    public static final String FAMILIES_CREATE = "families:create";
    public static final String FAMILIES_UPDATE = "families:update";
    public static final String FAMILIES_DELETE = "families:delete";

    public static final String SUPPLIERS_READ = "suppliers:read";
    public static final String SUPPLIERS_CREATE = "suppliers:create";
    public static final String SUPPLIERS_UPDATE = "suppliers:update";
    public static final String SUPPLIERS_DELETE = "suppliers:delete";
}
