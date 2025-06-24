package com.scprojectjava2.model;

/**
 * Enum que define los roles disponibles en el sistema
 */
public enum Role {
    ADMINISTRADOR("Administrador"),
    CAJERO("Cajero");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
