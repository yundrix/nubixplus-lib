package com.nubixplus.lib.domain.types;

public enum UserStatus {
    /** Puede iniciar sesion. */
    ACTIVE,
    /** Creado pero aun no confirma el correo. */
    PENDING_ACTIVATION,
    /** Desactivado por un administrador. */
    INACTIVE,
    /** Bloqueado por exceso de intentos fallidos. */
    LOCKED,
    /** Suspendido temporalmente. */
    SUSPENDED;

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
