package com.nubixplus.lib.domain.types;

/**
 * Estado del vinculo entre un usuario y una organizacion.
 */
public enum MembershipStatus {
    ACTIVE,
    PENDING,
    INACTIVE,
    REVOKED;

    public boolean canOperate() {
        return this == ACTIVE;
    }
}
