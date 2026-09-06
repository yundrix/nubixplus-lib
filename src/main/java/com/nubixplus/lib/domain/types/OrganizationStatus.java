package com.nubixplus.lib.domain.types;

public enum OrganizationStatus {
    ACTIVE,
    INACTIVE;

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
