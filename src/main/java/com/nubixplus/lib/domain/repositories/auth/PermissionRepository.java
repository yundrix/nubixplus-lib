package com.nubixplus.lib.domain.repositories.auth;

import com.nubixplus.lib.domain.entities.auth.Permission;
import com.nubixplus.lib.stereotype.BaseRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends BaseRepository<Permission> {

    Optional<Permission> findByCode(String code);

    List<Permission> findAllByCodeIn(Set<String> codes);

    List<Permission> findAllByModuleOrderByActionAsc(String module);

    List<Permission> findAllByOrderByModuleAscActionAsc();

    boolean existsByCode(String code);
}
