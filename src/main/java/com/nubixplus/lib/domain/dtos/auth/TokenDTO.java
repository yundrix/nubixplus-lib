package com.nubixplus.lib.domain.dtos.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.constants.SecurityConstants;
import com.nubixplus.lib.domain.dtos.organization.BaseOrganizationDTO;
import com.nubixplus.lib.domain.dtos.users.BaseUserDTO;
import com.nubixplus.lib.domain.entities.user.User;
import com.nubixplus.lib.domain.entities.user.UserOrganization;

import java.util.List;

/**
 * Respuesta del login. El {@code accessToken} queda amarrado a la organizacion del
 * RNC/cedula enviado; el header {@code X-Company-Document} de las peticiones
 * siguientes debe coincidir con {@code organization.documentNumber}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenDTO(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn,
        BaseUserDTO user,
        BaseOrganizationDTO organization,
        List<String> roles,
        List<String> permissions,
        boolean mustChangePassword
) {

    public static TokenDTO of(UserOrganization membership,
                              String accessToken,
                              String refreshToken,
                              long expiresIn) {
        final User user = membership.getUser();
        return new TokenDTO(
                SecurityConstants.TOKEN_TYPE,
                accessToken,
                refreshToken,
                expiresIn,
                BaseUserDTO.of(user),
                BaseOrganizationDTO.of(membership.getOrganization()),
                List.copyOf(membership.getRoleCodes()),
                List.copyOf(membership.getPermissionCodes()),
                user.isMustChangePassword());
    }
}
