--
-- Toda relacion N:M se modela con TRES tablas y una entidad intermedia propia
-- (role_permissions, user_organization_roles, product_suppliers). No hay
-- @ManyToMany en el modelo.
-- =============================================================================
-- NubixPlus - esquema base (MySQL 8.0+ / MariaDB 10.6+)
-- Equivalente a schema-postgresql.sql.
--
-- IMPORTANTE: los LocalDateTime se guardan como DATETIME(6) sin zona horaria. Hay que
-- fijar en cada servicio:
--     spring.jpa.properties.hibernate.jdbc.time_zone=UTC
-- para que todo se persista en UTC.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- permissions
-- -----------------------------------------------------------------------------
CREATE TABLE permissions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(300),
    module      VARCHAR(60)  NOT NULL,
    `action`    VARCHAR(60)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120),
    version     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code),
    INDEX ix_permissions_module (module)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- organizations
-- -----------------------------------------------------------------------------
CREATE TABLE organizations (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    legal_name      VARCHAR(200) NOT NULL,
    commercial_name VARCHAR(200),
    document_type   VARCHAR(20)  NOT NULL,
    document_number VARCHAR(20)  NOT NULL,
    email           VARCHAR(150),
    phone           VARCHAR(30),
    address         VARCHAR(300),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    timezone        VARCHAR(60)           DEFAULT 'America/Santo_Domingo',
    currency        VARCHAR(3)            DEFAULT 'DOP',
    logo_url        VARCHAR(500),
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_organizations_document_number UNIQUE (document_number),
    CONSTRAINT ck_organizations_document_type CHECK (document_type IN ('RNC', 'CEDULA')),
    INDEX ix_organizations_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- users
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    email                 VARCHAR(180) NOT NULL,
    password_hash         VARCHAR(200) NOT NULL,
    first_name            VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    phone                 VARCHAR(30),
    status                VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at         DATETIME(6),
    password_changed_at   DATETIME(6),
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    locked_until          DATETIME(6),
    avatar_url            VARCHAR(500),
    locale                VARCHAR(10),
    created_at            DATETIME(6)  NOT NULL,
    updated_at            DATETIME(6)  NOT NULL,
    created_by            VARCHAR(120),
    updated_by            VARCHAR(120),
    version               BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email  UNIQUE (email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'PENDING_ACTIVATION', 'INACTIVE', 'LOCKED', 'SUSPENDED')),
    INDEX ix_users_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
-- Con la collation utf8mb4_0900_ai_ci el UNIQUE ya es case-insensitive y las
-- busquedas por email ignoran mayusculas: no hace falta indice funcional.


-- -----------------------------------------------------------------------------
-- roles
-- -----------------------------------------------------------------------------
CREATE TABLE roles (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(60)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(300),
    organization_id BIGINT,
    system_role     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT       NOT NULL DEFAULT 0,
    -- Columna generada: MySQL no soporta indices parciales. Sirve para garantizar
    -- la unicidad del code entre los roles de sistema (organization_id NULL).
    global_code     VARCHAR(60)  GENERATED ALWAYS AS (CASE WHEN organization_id IS NULL THEN code END) STORED,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_organization_code UNIQUE (organization_id, code),
    CONSTRAINT uk_roles_code_global       UNIQUE (global_code),
    CONSTRAINT fk_roles_organization      FOREIGN KEY (organization_id) REFERENCES organizations (id),
    INDEX ix_roles_code (code),
    INDEX ix_roles_organization (organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- role_permissions  (RolePermission: tabla intermedia EXPLICITA)
-- -----------------------------------------------------------------------------
CREATE TABLE role_permissions (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    role_id       BIGINT      NOT NULL,
    permission_id BIGINT      NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_permissions            UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role       FOREIGN KEY (role_id)       REFERENCES roles (id)       ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id),
    INDEX ix_role_permissions_permission (permission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- user_organizations
-- -----------------------------------------------------------------------------
CREATE TABLE user_organizations (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    user_id         BIGINT      NOT NULL,
    organization_id BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    default_organization BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at       DATETIME(6),
    job_title       VARCHAR(120),
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT      NOT NULL DEFAULT 0,
    -- Equivalente al indice parcial de PostgreSQL: una sola org por defecto por usuario.
    default_user_id BIGINT      GENERATED ALWAYS AS (CASE WHEN default_organization THEN user_id END) STORED,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_organizations_user_org     UNIQUE (user_id, organization_id),
    CONSTRAINT uk_user_organizations_default      UNIQUE (default_user_id),
    CONSTRAINT fk_user_organizations_user         FOREIGN KEY (user_id)         REFERENCES users (id),
    CONSTRAINT fk_user_organizations_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT ck_user_organizations_status       CHECK (status IN ('ACTIVE', 'PENDING', 'INACTIVE', 'REVOKED')),
    INDEX ix_user_organizations_organization (organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- user_organization_roles  (UserOrganizationRole: tabla intermedia EXPLICITA)
-- -----------------------------------------------------------------------------
CREATE TABLE user_organization_roles (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    user_organization_id BIGINT      NOT NULL,
    role_id              BIGINT      NOT NULL,
    created_at           DATETIME(6) NOT NULL,
    updated_at           DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_organization_roles        UNIQUE (user_organization_id, role_id),
    CONSTRAINT fk_user_organization_roles_member FOREIGN KEY (user_organization_id) REFERENCES user_organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_organization_roles_role   FOREIGN KEY (role_id)              REFERENCES roles (id),
    INDEX ix_user_organization_roles_role (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- refresh_tokens
-- -----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    token_hash      VARCHAR(128) NOT NULL,
    user_id         BIGINT       NOT NULL,
    organization_id BIGINT       NOT NULL,
    issued_at       DATETIME(6)  NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    revoked         BOOLEAN      NOT NULL DEFAULT FALSE,
    revoked_at      DATETIME(6),
    user_agent      VARCHAR(300),
    ip_address      VARCHAR(60),
    -- BaseEntity
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash         UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user         FOREIGN KEY (user_id)         REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_refresh_tokens_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    INDEX ix_refresh_tokens_expires_at (expires_at),
    INDEX ix_refresh_tokens_user_org (user_id, organization_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- =============================================================================
-- MODULO DE INVENTARIO
--
-- Jerarquia:               families 1:N categories 1:N products
-- Codigos del producto:    products 1:N product_codes
-- Relacion N:M del modulo: products 1:N product_suppliers N:1 suppliers
--
-- Todo el catalogo es POR COMPANIA: los codigos son unicos dentro de la
-- organizacion, no en toda la plataforma. La disponibilidad binaria va como boolean
-- 'active'; solo products conserva un enum 'status' (tres estados).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- families
-- -----------------------------------------------------------------------------
CREATE TABLE families (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(30)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(300),
    organization_id BIGINT       NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_families_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_families_organization      FOREIGN KEY (organization_id) REFERENCES organizations (id),
    INDEX ix_families_code (code),
    INDEX ix_families_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- categories
-- -----------------------------------------------------------------------------
CREATE TABLE categories (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    code            VARCHAR(30)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(300),
    family_id       BIGINT       NOT NULL,
    organization_id BIGINT       NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_organization_code UNIQUE (organization_id, code),
    CONSTRAINT fk_categories_family            FOREIGN KEY (family_id)       REFERENCES families (id),
    CONSTRAINT fk_categories_organization      FOREIGN KEY (organization_id) REFERENCES organizations (id),
    INDEX ix_categories_family (family_id),
    INDEX ix_categories_code (code),
    INDEX ix_categories_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- suppliers
-- -----------------------------------------------------------------------------
CREATE TABLE suppliers (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    code               VARCHAR(30)  NOT NULL,
    legal_name         VARCHAR(200) NOT NULL,
    commercial_name    VARCHAR(200),
    document_type      VARCHAR(20),
    document_number    VARCHAR(20),
    email              VARCHAR(150),
    phone              VARCHAR(30),
    address            VARCHAR(300),
    contact_name       VARCHAR(150),
    payment_terms_days INT,
    organization_id    BIGINT       NOT NULL,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    created_by         VARCHAR(120),
    updated_by         VARCHAR(120),
    version            BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_suppliers_organization_code     UNIQUE (organization_id, code),
    CONSTRAINT uk_suppliers_organization_document UNIQUE (organization_id, document_number),
    CONSTRAINT fk_suppliers_organization          FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT ck_suppliers_document_type         CHECK (document_type IS NULL OR document_type IN ('RNC', 'CEDULA')),
    INDEX ix_suppliers_document_number (document_number),
    INDEX ix_suppliers_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- products
--
-- Sin columnas de codigo: los codigos viven en product_codes, uno por fila.
-- -----------------------------------------------------------------------------
CREATE TABLE products (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)  NOT NULL,
    description     VARCHAR(500),
    brand           VARCHAR(120),
    category_id     BIGINT        NOT NULL,
    organization_id BIGINT        NOT NULL,
    unit            VARCHAR(20)   NOT NULL DEFAULT 'UNIT',
    cost            DECIMAL(19,4),
    price           DECIMAL(19,4),
    track_stock     BOOLEAN       NOT NULL DEFAULT TRUE,
    min_stock       DECIMAL(19,4),
    max_stock       DECIMAL(19,4),
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category     FOREIGN KEY (category_id)     REFERENCES categories (id),
    CONSTRAINT fk_products_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT ck_products_status       CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED')),
    CONSTRAINT ck_products_unit         CHECK (unit IN ('UNIT', 'PACK', 'BOX', 'DOZEN', 'GRAM', 'KILOGRAM',
                                                        'POUND', 'MILLILITER', 'LITER', 'GALLON', 'METER', 'FOOT')),
    INDEX ix_products_organization (organization_id),
    INDEX ix_products_category (category_id),
    INDEX ix_products_name (name),
    INDEX ix_products_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- product_codes  (ProductCode: los codigos del producto, uno por fila)
--
-- Unicidad (organization_id, type, code_value): por compania porque el catalogo es
-- de cada tenant, y por tipo porque cada tipo es un espacio de nombres distinto.
-- -----------------------------------------------------------------------------
CREATE TABLE product_codes (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    product_id      BIGINT       NOT NULL,
    organization_id BIGINT       NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    code_value      VARCHAR(60)  NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_codes_organization_type_value UNIQUE (organization_id, type, code_value),
    CONSTRAINT fk_product_codes_product      FOREIGN KEY (product_id)      REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_codes_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT ck_product_codes_type         CHECK (type IN ('EAN8', 'EAN13', 'EAN14', 'UPC12', 'UPC6', 'SKU', 'CUSTOM')),
    INDEX ix_product_codes_product (product_id),
    INDEX ix_product_codes_value (code_value),
    INDEX ix_product_codes_type (type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------------------------------
-- product_suppliers  (ProductSupplier: tabla intermedia EXPLICITA)
-- -----------------------------------------------------------------------------
CREATE TABLE product_suppliers (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    product_id     BIGINT      NOT NULL,
    supplier_id    BIGINT      NOT NULL,
    supplier_reference   VARCHAR(60),
    last_cost      DECIMAL(19,4),
    lead_time_days INT,
    preferred      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    -- Equivalente al indice parcial de PostgreSQL: un solo suplidor preferido por producto.
    preferred_product_id BIGINT GENERATED ALWAYS AS (CASE WHEN preferred THEN product_id END) STORED,
    PRIMARY KEY (id),
    CONSTRAINT uk_product_suppliers           UNIQUE (product_id, supplier_id),
    CONSTRAINT uk_product_suppliers_preferred UNIQUE (preferred_product_id),
    CONSTRAINT fk_product_suppliers_product   FOREIGN KEY (product_id)  REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_suppliers_supplier  FOREIGN KEY (supplier_id) REFERENCES suppliers (id),
    INDEX ix_product_suppliers_supplier (supplier_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
