package com.nubixplus.lib.domain.repositories.auth;

import com.nubixplus.lib.domain.entities.auth.Role;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends BaseRepository<Role> {

    /** Rol de sistema (sin organizacion). */
    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByCodeAndOrganizationIsNullAndDeletedFalse(String code);

    Optional<Role> findByCodeAndOrganizationIdAndDeletedFalse(String code, Long organizationId);

    Optional<Role> findByIdAndDeletedFalse(Long id);

    /** Roles visibles para una organizacion: los de sistema mas los propios. */
    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select distinct r from Role r
             where r.deleted = false
               and (r.organization is null or r.organization.id = :organizationId)
             order by r.code
            """)
    List<Role> findAvailableForOrganization(@Param("organizationId") Long organizationId);

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select distinct r from Role r
             where r.deleted = false
               and r.id in :ids
               and (r.organization is null or r.organization.id = :organizationId)
            """)
    List<Role> findAllByIdsForOrganization(@Param("ids") Set<Long> ids,
                                           @Param("organizationId") Long organizationId);

    boolean existsByCodeAndOrganizationIdAndDeletedFalse(String code, Long organizationId);

    default Optional<Role> findSystemRole(String code) {
        return findByCodeAndOrganizationIsNullAndDeletedFalse(code);
    }
}
