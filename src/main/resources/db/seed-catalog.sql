-- =============================================================================
-- NubixPlus - datos base del catalogo de seguridad (PostgreSQL / MySQL)
--
-- Carga los permisos de PermissionCodes y los roles de sistema de RoleCodes
-- (organization_id NULL, system_role = true) con su matriz de permisos.
-- Es idempotente: se puede volver a ejecutar sin duplicar.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Permisos (com.nubixplus.lib.common.constant.PermissionCodes)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (code, name, description, module, action, created_at, created_by, version, deleted)
SELECT v.code, v.name, v.description, v.module, v.action, CURRENT_TIMESTAMP, 'system', 0, FALSE
FROM (
    SELECT 'users:read'           AS code, 'Ver usuarios'          AS name, 'Consultar usuarios de la organizacion'      AS description, 'users'         AS module, 'read'   AS action
    UNION ALL SELECT 'users:create',         'Crear usuarios',        'Registrar nuevos usuarios en la organizacion',       'users',         'create'
    UNION ALL SELECT 'users:update',         'Editar usuarios',       'Modificar datos y estado de usuarios',               'users',         'update'
    UNION ALL SELECT 'users:delete',         'Eliminar usuarios',     'Dar de baja usuarios de la organizacion',            'users',         'delete'
    UNION ALL SELECT 'roles:read',           'Ver roles',             'Consultar roles disponibles',                        'roles',         'read'
    UNION ALL SELECT 'roles:create',         'Crear roles',           'Crear roles propios de la organizacion',             'roles',         'create'
    UNION ALL SELECT 'roles:update',         'Editar roles',          'Modificar roles propios de la organizacion',         'roles',         'update'
    UNION ALL SELECT 'roles:delete',         'Eliminar roles',        'Eliminar roles propios de la organizacion',          'roles',         'delete'
    UNION ALL SELECT 'permissions:read',     'Ver permisos',          'Consultar el catalogo de permisos',                  'permissions',   'read'
    UNION ALL SELECT 'organizations:read',   'Ver organizacion',      'Consultar los datos de la organizacion',             'organizations', 'read'
    UNION ALL SELECT 'organizations:update', 'Editar organizacion',   'Modificar los datos de la organizacion',             'organizations', 'update'
    UNION ALL SELECT 'organizations:create', 'Crear organizaciones',  'Registrar nuevas organizaciones en la plataforma',   'organizations', 'create'
) v
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = v.code);


-- -----------------------------------------------------------------------------
-- 2. Roles de sistema (com.nubixplus.lib.common.constant.RoleCodes)
-- -----------------------------------------------------------------------------
INSERT INTO roles (code, name, description, organization_id, system_role, created_at, created_by, version, deleted)
SELECT v.code, v.name, v.description, NULL, TRUE, CURRENT_TIMESTAMP, 'system', 0, FALSE
FROM (
    SELECT 'SUPER_ADMIN' AS code, 'Super administrador'        AS name, 'Acceso total a la plataforma, incluso entre organizaciones' AS description
    UNION ALL SELECT 'ORG_ADMIN',  'Administrador de compania',        'Gestiona usuarios, roles y configuracion de su organizacion'
    UNION ALL SELECT 'USER',       'Usuario',                          'Usuario operativo estandar'
    UNION ALL SELECT 'VIEWER',     'Consulta',                         'Acceso de solo lectura'
) v
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.code = v.code AND r.organization_id IS NULL);


-- -----------------------------------------------------------------------------
-- 3. Matriz rol -> permisos
-- -----------------------------------------------------------------------------

-- SUPER_ADMIN: todos los permisos.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ORG_ADMIN: todo dentro de su organizacion, menos crear organizaciones.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code <> 'organizations:create'
WHERE r.code = 'ORG_ADMIN'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- USER: lectura general.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('users:read', 'roles:read', 'organizations:read')
WHERE r.code = 'USER'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- VIEWER: solo lectura, cualquier modulo.
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.action = 'read'
WHERE r.code = 'VIEWER'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);