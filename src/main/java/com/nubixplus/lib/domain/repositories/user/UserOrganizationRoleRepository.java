package com.nubixplus.lib.domain.repositories.user;

import com.nubixplus.lib.domain.entities.user.UserOrganizationRole;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

/**
 * Tabla intermedia {@code user_organization_roles}. Ver {@code RolePermissionRepository}.
 */
public interface UserOrganizationRoleRepository extends BaseRepository<UserOrganizationRole> {

    /** El rol viene resuelto desde la consulta: los servicios corren sin open-in-view. */
    @EntityGraph(attributePaths = "role")
    List<UserOrganizationRole> findAllByUserOrganizationId(Long userOrganizationId);

    List<UserOrganizationRole> findAllByRoleId(Long roleId);

    Optional<UserOrganizationRole> findByUserOrganizationIdAndRoleId(Long userOrganizationId, Long roleId);

    boolean existsByUserOrganizationIdAndRoleId(Long userOrganizationId, Long roleId);

    long countByUserOrganizationId(Long userOrganizationId);

    /** Impide borrar un rol que todavia tiene gente asignada. */
    long countByRoleId(Long roleId);

    void deleteByUserOrganizationIdAndRoleId(Long userOrganizationId, Long roleId);
}
