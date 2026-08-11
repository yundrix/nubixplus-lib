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
│   ├── AuditableEntity    + createdBy, updatedBy, @Version y borrado lógico
│   ├── BaseRepository     JpaRepository + JpaSpecificationExecutor
│   ├── BaseService        contrato CRUD genérico
│   ├── DefaultBaseService implementación; las subclases solo aportan getRepository()
│   └── TransactionId      id de correlación en el MDC
├── domain
│   ├── constants   AppConstants, SecurityConstants, RoleCodes, PermissionCodes
│   ├── dtos        BaseDTO + auth/, organization/, users/
│   ├── entities    auth/, organization/, user/
│   ├── exceptions  RestResponseException + especializaciones + model/
│   ├── repositories  un repositorio por entidad, agrupados por feature
│   └── types       DocumentType, OrganizationStatus, UserStatus, MembershipStatus
└── utils           Documents (RNC/cédula), Crypto (hashes)
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

```
users ──┐                          ┌── organizations
        │                          │      (RNC o cédula, único)
        └──< user_organizations >──┘
                    │
                    └──< user_organization_roles >── roles ──< role_permissions >── permissions
```

Un usuario (correo + contraseña globales) pertenece a **N organizaciones** vía
`user_organizations`. Los roles se asignan **por membresía**, no por usuario: alguien puede
ser `ORG_ADMIN` en una compañía y `VIEWER` en otra.

`roles.organization_id` en `NULL` = rol de sistema disponible para todas las compañías.

El DDL de referencia para PostgreSQL y MySQL está en `src/main/resources/db/`.

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
- **Borrado lógico**: `AuditableEntity.deleted`. Los repositorios filtran con `...AndDeletedFalse`.
- **Colecciones como `Set`**, nunca `List`, para evitar `MultipleBagFetchException` al hacer
  varios `join fetch` en la consulta del login.
- Las entidades exponen constantes `FIELD_*` con el nombre de sus atributos, que es lo que
  consumen las `Specification` del servicio en vez de literales sueltos.
- Se publica también el `sources.jar`.
