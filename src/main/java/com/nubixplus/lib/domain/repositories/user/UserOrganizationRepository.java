package com.nubixplus.lib.domain.repositories.user;

import com.nubixplus.lib.domain.entities.user.UserOrganization;
import com.nubixplus.lib.stereotype.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserOrganizationRepository extends BaseRepository<UserOrganization> {

    /**
     * Los servicios corren con {@code open-in-view: false}, asi que el grafo que los
     * DTO van a leer tiene que venir resuelto desde la consulta: fuera de la
     * transaccion ya no hay sesion para inicializar nada perezoso.
     */
    @Override
    @EntityGraph(attributePaths = {"user", "roles", "roles.permissions"})
    Page<UserOrganization> findAll(Specification<UserOrganization> specification, Pageable pageable);

    /**
     * Consulta principal del login: trae la membresia con usuario, organizacion,
     * roles y permisos en un solo viaje a la base de datos.
     *
     * <p>Devuelve {@code List} en lugar de {@code Optional} porque los fetch joins
     * sobre colecciones pueden producir filas duplicadas segun el dialecto.</p>
     */
    @Query("""
            select m from UserOrganization m
              join fetch m.user u
              join fetch m.organization o
              left join fetch m.roles r
              left join fetch r.permissions p
             where lower(u.email) = lower(:email)
               and o.documentNumber = :documentNumber
               and m.deleted = false
               and u.deleted = false
               and o.deleted = false
            """)
    List<UserOrganization> findMembershipByEmailAndDocument(@Param("email") String email,
                                                           @Param("documentNumber") String documentNumber);

    default Optional<UserOrganization> findMembership(String email, String documentNumber) {
        return findMembershipByEmailAndDocument(email, documentNumber).stream().findFirst();
    }

    @Query("""
            select m from UserOrganization m
              join fetch m.user u
              join fetch m.organization o
              left join fetch m.roles r
              left join fetch r.permissions p
             where u.id = :userId
               and o.id = :organizationId
               and m.deleted = false
            """)
    List<UserOrganization> findMembershipByUserAndOrganizationId(@Param("userId") Long userId,
                                                                 @Param("organizationId") Long organizationId);

    default Optional<UserOrganization> findMembership(Long userId, Long organizationId) {
        return findMembershipByUserAndOrganizationId(userId, organizationId).stream().findFirst();
    }

    /** Organizaciones a las que pertenece un usuario (para pantallas de seleccion). */
    @Query("""
            select distinct m from UserOrganization m
              join fetch m.organization o
             where lower(m.user.email) = lower(:email)
               and m.deleted = false
               and o.deleted = false
            """)
    List<UserOrganization> findAllByUserEmail(@Param("email") String email);

    @Query(value = """
            select m from UserOrganization m
              join fetch m.user u
             where m.organization.id = :organizationId
               and m.deleted = false
               and u.deleted = false
            """,
            countQuery = """
                    select count(m) from UserOrganization m
                     where m.organization.id = :organizationId
                       and m.deleted = false
                       and m.user.deleted = false
                    """)
    Page<UserOrganization> findAllByOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);

    boolean existsByUserIdAndOrganizationIdAndDeletedFalse(Long userId, Long organizationId);
}
