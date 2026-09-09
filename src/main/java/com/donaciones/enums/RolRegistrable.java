package com.donaciones.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RolRegistrable {

    DONANTE("Donante"),
    PERSONAL_APOYO("Personal de Apoyo");

    private final String nombreRol;

    RolRegistrable(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    @JsonCreator
    public static RolRegistrable fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RolRegistrable rol : values()) {
            if (rol.name().equalsIgnoreCase(value)) {
                return rol;
            }
        }
        return null;
    }

}