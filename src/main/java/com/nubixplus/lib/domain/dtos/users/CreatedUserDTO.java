package com.nubixplus.lib.domain.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resultado del alta de un usuario.
 *
 * @param temporaryPassword solo viene cuando el administrador no envio contrasena y
 *                          el sistema genero una temporal. Es la unica vez que se
 *                          muestra; el usuario esta obligado a cambiarla al entrar.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatedUserDTO(UserDetailsDTO user, String temporaryPassword) {

    public static CreatedUserDTO of(UserDetailsDTO user, String temporaryPassword) {
        return new CreatedUserDTO(user, temporaryPassword);
    }
}
