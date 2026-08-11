package com.nubixplus.lib.domain.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.organization.BaseOrganizationDTO;
import com.nubixplus.lib.domain.dtos.users.BaseUserDTO;
import com.nubixplus.lib.domain.dtos.users.UserOrganizationDTO;
import com.nubixplus.lib.domain.entities.user.UserOrganization;

import java.util.Collection;
import java.util.List;

/** Respuesta de {@code GET /auth/me}: quien soy y en que compania estoy operando. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionDTO(
        BaseUserDTO user,
        BaseOrganizationDTO organization,
        List<String> roles,
        List<String> permissions,
        List<UserOrganizationDTO> organizations
) {

    public static SessionDTO of(UserOrganization membership, Collection<UserOrganization> memberships) {
        return new SessionDTO(
                BaseUserDTO.of(membership.getUser()),
                BaseOrganizationDTO.of(membership.getOrganization()),
                List.copyOf(membership.getRoleCodes()),
                List.copyOf(membership.getPermissionCodes()),
                UserOrganizationDTO.of(memberships));
    }
}
