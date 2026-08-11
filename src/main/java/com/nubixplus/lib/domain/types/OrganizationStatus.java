package com.nubixplus.lib.domain.types;

public enum OrganizationStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
