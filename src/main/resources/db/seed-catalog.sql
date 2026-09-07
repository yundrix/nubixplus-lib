-- =============================================================================
-- NubixPlus - datos base del catalogo de seguridad (PostgreSQL / MySQL)
--
-- Carga los permisos de PermissionCodes y los roles de sistema de RoleCodes
-- (organization_id NULL, system_role = true) con su matriz de permisos.
-- Es idempotente: se puede volver a ejecutar sin duplicar.
--
-- Equivale a lo que hace SecurityDataSeeder al arrancar el servicio; sirve para los
-- despliegues donde el seed viene desactivado (server.bootstrap.seed-security=false).
--
-- role_permissions es una tabla intermedia EXPLICITA (entidad RolePermission), asi
-- que sus filas llevan created_at / updated_at como cualquier otra entidad.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Permisos (com.nubixplus.lib.domain.constants.PermissionCodes)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (code, name, description, module, action, created_at, updated_at, created_by, version)
SELECT v.code, v.name, v.description, v.module, v.action, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 0
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
    -- Inventario
    UNION ALL SELECT 'products:read',        'Ver productos',         'Consultar el catalogo de productos',                 'products',      'read'
    UNION ALL SELECT 'products:create',      'Crear productos',       'Registrar productos en el catalogo',                 'products',      'create'
    UNION ALL SELECT 'products:update',      'Editar productos',      'Modificar productos del catalogo',                   'products',      'update'
    UNION ALL SELECT 'products:delete',      'Eliminar productos',    'Dar de baja productos del catalogo',                 'products',      'delete'
    UNION ALL SELECT 'categories:read',      'Ver categorias',        'Consultar las categorias del catalogo',              'categories',    'read'
    UNION ALL SELECT 'categories:create',    'Crear categorias',      'Crear categorias del catalogo',                      'categories',    'create'
    UNION ALL SELECT 'categories:update',    'Editar categorias',     'Modificar categorias del catalogo',                  'categories',    'update'
    UNION ALL SELECT 'categories:delete',    'Eliminar categorias',   'Dar de baja categorias del catalogo',                'categories',    'delete'
    UNION ALL SELECT 'families:read',        'Ver familias',          'Consultar las familias del catalogo',                'families',      'read'
    UNION ALL SELECT 'families:create',      'Crear familias',        'Crear familias del catalogo',                        'families',      'create'
    UNION ALL SELECT 'families:update',      'Editar familias',       'Modificar familias del catalogo',                    'families',      'update'
    UNION ALL SELECT 'families:delete',      'Eliminar familias',     'Dar de baja familias del catalogo',                  'families',      'delete'
    UNION ALL SELECT 'suppliers:read',       'Ver suplidores',        'Consultar los suplidores de la organizacion',        'suppliers',     'read'
    UNION ALL SELECT 'suppliers:create',     'Crear suplidores',      'Registrar suplidores de la organizacion',            'suppliers',     'create'
    UNION ALL SELECT 'suppliers:update',     'Editar suplidores',     'Modificar suplidores de la organizacion',            'suppliers',     'update'
    UNION ALL SELECT 'suppliers:delete',     'Eliminar suplidores',   'Dar de baja suplidores de la organizacion',          'suppliers',     'delete'
) v
WHERE NOT EXISTS (SELECT 1 FROM permissions p WHERE p.code = v.code);


-- -----------------------------------------------------------------------------
-- 2. Roles de sistema (com.nubixplus.lib.domain.constants.RoleCodes)
-- -----------------------------------------------------------------------------
INSERT INTO roles (code, name, description, organization_id, system_role, created_at, updated_at, created_by, version)
SELECT v.code, v.name, v.description, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 0
FROM (
    SELECT 'SUPER_ADMIN' AS code, 'Super administrador'        AS name, 'Acceso total a la plataforma, incluso entre organizaciones' AS description
    UNION ALL SELECT 'ORG_ADMIN',  'Administrador de compania',        'Gestiona usuarios, roles y configuracion de su organizacion'
    UNION ALL SELECT 'USER',       'Usuario',                          'Usuario operativo estandar'
    UNION ALL SELECT 'VIEWER',     'Consulta',                         'Acceso de solo lectura'
) v
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.code = v.code AND r.organization_id IS NULL);


-- -----------------------------------------------------------------------------
-- 3. Matriz rol -> permisos (tabla intermedia explicita role_permissions)
-- -----------------------------------------------------------------------------

-- SUPER_ADMIN: todos los permisos.
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ORG_ADMIN: todo dentro de su organizacion, menos crear organizaciones.
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM roles r
JOIN permissions p ON p.code <> 'organizations:create'
WHERE r.code = 'ORG_ADMIN'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- USER: lectura general mas la operacion diaria del inventario.
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM roles r
JOIN permissions p ON p.code IN ('users:read', 'roles:read', 'organizations:read',
                                 'products:read', 'products:create', 'products:update',
                                 'categories:read', 'families:read', 'suppliers:read')
WHERE r.code = 'USER'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- VIEWER: solo lectura, cualquier modulo.
INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM roles r
JOIN permissions p ON p.action = 'read'
WHERE r.code = 'VIEWER'
  AND r.organization_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);
