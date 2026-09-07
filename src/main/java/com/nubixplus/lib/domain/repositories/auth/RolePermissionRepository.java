package com.nubixplus.lib.domain.repositories.auth;

import com.nubixplus.lib.domain.entities.auth.RolePermission;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Tabla intermedia {@code role_permissions}. Existe como repositorio propio porque la
 * relacion N:M es explicita: la fila del medio es una entidad como cualquier otra.
 */
public interface RolePermissionRepository extends BaseRepository<RolePermission> {

    /**
     * Con {@code open-in-view: false} el permiso tiene que venir resuelto desde la
     * consulta: fuera de la transaccion ya no hay sesion para inicializar el proxy.
     */
    @EntityGraph(attributePaths = "permission")
    List<RolePermission> findAllByRoleId(Long roleId);

    @EntityGraph(attributePaths = "role")
    List<RolePermission> findAllByPermissionId(Long permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    long countByRoleId(Long roleId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
