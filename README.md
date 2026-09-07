# nubixplus-lib

Librería base compartida por todos los servicios de NubixPlus. Se publica en Nexus y los
servicios la consumen como dependencia. Sigue el mismo patrón que `aisec-lib`.

```xml
<dependency>
    <groupId>com.nubixplus</groupId>
    <artifactId>nubixplus-lib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Qué vive aquí (y qué no)

| Aquí (lib) | En el servicio |
|---|---|
| `stereotype`: BaseEntity, BaseRepository, BaseService | Implementaciones `Default*` de los servicios |
| Entidades JPA | Resources (controllers) |
| Repositorios Spring Data | Configuración de seguridad, filtros, JWT |
| DTOs de respuesta (con `of(entity)`) | DTOs de request (`domain/request`) |
| Excepciones y su modelo de error | `@RestControllerAdvice` |
| Constantes, enums y utilidades | Specifications, seeders y jobs |

Regla práctica: si dos servicios lo van a necesitar, va en la lib.

## Estructura

```
com.nubixplus.lib
├── stereotype
│   ├── BaseEntity         id + createdAt/updatedAt (@PrePersist / @PreUpdate)
│   ├── AuditableEntity    + createdBy, updatedBy y @Version
│   ├── BaseRepository     JpaRepository + JpaSpecificationExecutor
│   ├── BaseService        contrato CRUD genérico
│   ├── DefaultBaseService implementación; las subclases solo aportan getRepository()
│   └── TransactionId      id de correlación en el MDC
├── domain
│   ├── constants   AppConstants, SecurityConstants, RoleCodes, PermissionCodes
│   ├── dtos        BaseDTO + auth/, organization/, users/, inventory/
│   ├── entities    auth/, organization/, user/, inventory/
│   ├── exceptions  RestResponseException + especializaciones + model/
│   ├── repositories  un repositorio por entidad, agrupados por feature
│   └── types       DocumentType, UserStatus, MembershipStatus,
│                   ProductStatus, ProductCodeType, UnitOfMeasure
└── utils           Documents (RNC/cédula), Crypto (hashes), ProductCodes (SKU/GTIN)
```

## El patrón de servicio

Un servicio del dominio se arma en dos piezas, igual que en aisec:

```java
public interface OrganizationService extends BaseService<Organization> {
    Optional<Organization> findByDocument(String document);
}

@Getter
@Service
@AllArgsConstructor
public class DefaultOrganizationService extends DefaultBaseService<Organization>
                                        implements OrganizationService {
    private final OrganizationRepository repository;   // lo toma el getRepository() de Lombok
}
```

`DefaultBaseService` ya resuelve `save`, `create`, `findById` (lanza `NotFoundException` con el
nombre de la entidad), `getById` y las variantes de `findAll` con `Pageable` y `Specification`.

## Los DTOs

No hay clases mapper: cada DTO sabe construirse desde su entidad, con un constructor protegido y
un `of(...)` estático que tolera nulos.

```java
UserDetailsDTO.of(membership);          // null-safe
RoleDTO.of(role.getPermissions());      // colecciones, ya ordenadas
```

Se apilan de menos a más detalle — `BaseUserDTO` → `UserDTO` → `UserDetailsDTO` — para que cada
endpoint devuelva solo lo que necesita.

## Modelo de datos

### Seguridad

```
users ──1:N── user_organizations ──N:1── organizations
                    │                        (RNC o cédula, único)
                   1:N
                    │
         user_organization_roles ──N:1── roles ──1:N── role_permissions ──N:1── permissions
```

Un usuario (correo + contraseña globales) pertenece a **N organizaciones** vía
`user_organizations`. Los roles se asignan **por membresía**, no por usuario: alguien puede
ser `ORG_ADMIN` en una compañía y `VIEWER` en otra.

`roles.organization_id` en `NULL` = rol de sistema disponible para todas las compañías.

### Inventario

```
                                     brands
                                        │ 1:N  (opcional)
                                        │
families ──1:N── categories ──1:N── products ──1:N── product_codes
                                        │              (SKU, UPC12, EAN13, ...)
                                       1:N
                                        │
                             product_suppliers ──N:1── suppliers
```

Todo el catálogo es **por compañía**: los códigos son únicos dentro de la organización, no en
toda la plataforma. Dos compañías pueden vender el mismo artículo y comprarle al mismo
proveedor.

El DDL de referencia para PostgreSQL y MySQL está en `src/main/resources/db/`, y las
migraciones versionadas en `src/main/resources/db/migration/<motor>/`:

| Migración | Qué hace |
|---|---|
| `V0001` | Convierte las tablas intermedias de `@ManyToMany` en entidades (`id` + timestamps) sin perder filas |
| `V0002` | Crea el módulo de inventario |
| `V0003` | `organizations.status` → `active`, `is_default` → `default_organization` |
| `V0004` | Saca `deleted` de todas las tablas: borrar vuelve a ser borrar |
| `V0005` | Alinea la base de dev con el DDL de referencia (solo PostgreSQL) |
| `V0006` | `products.brand` (texto) → tabla `brands` + `products.brand_id`, con los valores ya cargados migrados |

Los nombres siguen la convención de Flyway por si más adelante se adopta.

## Cómo la usa un servicio

Las entidades y repositorios están fuera del paquete de la app, así que hay que indicárselo
a Boot (ver `LibConfig` en nubixplus-services):

```java
@Configuration
@EntityScan(basePackages = "com.nubixplus")
@ComponentScan(basePackages = "com.nubixplus")
@EnableJpaRepositories(basePackages = "com.nubixplus")
public class LibConfig {
}
```

`AuditableEntity` requiere además un bean `AuditorAware<String>` para llenar `created_by` /
`updated_by`.

## Publicar en Nexus

```bash
mvn clean deploy                       # usa las URLs del <distributionManagement>
mvn clean deploy -Dnexus.snapshots.url=https://otro-nexus/repository/maven-snapshots/
```

Las credenciales van en `~/.m2/settings.xml` con los ids `nexus-releases` / `nexus-snapshots`
(ver `docs/settings.xml.example` en nubixplus-services).

Para probar cambios sin publicar: `mvn clean install` deja el artefacto en el `.m2` local y el
servicio lo toma de ahí.

## Notas de diseño

- **Sin `<parent>` de Boot.** Solo se importa su BOM: una librería no debe heredar el ciclo de
  vida de la aplicación. Tampoco usa `spring-boot-maven-plugin`, cuyo `repackage` generaría un
  fat-jar ejecutable que no sirve como dependencia.
- **`RestResponseException`** guarda el `HttpStatus` y sabe convertirse en `ErrorResponse`, así el
  `@RestControllerAdvice` del servicio traduce cualquier excepción sin conocer el caso concreto.
- **PK `Long` con `IDENTITY`** (`BIGSERIAL` en Postgres).
- **Fechas como `LocalDateTime`**, gestionadas por `BaseEntity` en `@PrePersist` / `@PreUpdate`.
- **Sin borrado lógico.** Borrar es borrar: un `DELETE` de verdad. Que una fila cuente o no ya
  lo dice su propio estado (`active` / `status`), y tener además un `deleted` era decir lo mismo
  dos veces. Lo que no se puede borrar por dejar referencias colgando se corta antes, con un
  mensaje que dice qué hay que soltar primero.
- **Colecciones como `Set`**, nunca `List`, para evitar `MultipleBagFetchException` al hacer
  varios `join fetch` en la misma consulta (el login trae roles y permisos; el producto trae
  códigos y suplidores).
- **Nada de `@ManyToMany`.** Toda relación N:M se modela con tres tablas y una entidad
  intermedia propia: `RolePermission`, `UserOrganizationRole`, `ProductSupplier`. La fila del
  medio tiene su `id`, sus timestamps y su repositorio, y la unicidad del par la garantiza un
  `UNIQUE` sobre las dos FK. Las tres extienden `BaseEntity` y no `AuditableEntity`: un
  vínculo se otorga o se revoca, y no necesita ni autor ni versión.
- **Estados.** Disponibilidad binaria → `boolean active` (`organizations`, `families`,
  `categories`, `brands`, `suppliers`). Ciclo de vida con más de dos estados y comportamiento distinto
  en cada uno → enum `status` (`users`, `user_organizations`, `products`). No se usan enums de
  dos valores: no aportan nada sobre el boolean y arrastran un `CHECK` que hay que mantener.
- **Códigos del producto como entidad.** `ProductCode` (1:N desde `Product`) en vez de
  columnas `sku`/`upc`/`ean`: un artículo acumula códigos de distinto tipo y con columnas
  fijas cada tipo nuevo sería una migración. La unicidad es `(organización, tipo, valor)` —
  por compañía porque el catálogo es de cada tenant, y por tipo porque cada tipo es un
  espacio de nombres distinto. Las reglas de formato de cada `ProductCodeType` (largo y
  dígito verificador GTIN) viven en un solo lugar: `ProductCodes`.
- **Marca como entidad.** `Brand` (N:1 desde `Product`) en vez de un `VARCHAR` en el producto:
  como texto libre, `Nestlé`, `NESTLE` y `Nestle ` eran tres marcas distintas para el reporte y
  no había dónde corregirlas de una sola vez. El código es único por compañía, igual que en
  familias y categorías. La FK admite `NULL` a propósito: hay artículos genéricos o de
  producción propia que no tienen marca.
- Las entidades exponen constantes `FIELD_*` con el nombre de sus atributos, que es lo que
  consumen las `Specification` del servicio en vez de literales sueltos.
- Se publica también el `sources.jar`.
