package com.driagon.services.healthcheck.constants;

public enum HealthStatus {
    UP("UP", "El servicio está funcionando correctamente"),
    DOWN("DOWN", "El servicio no está disponible"),
    UNKNOWN("UNKNOWN", "No se puede determinar el estado del servicio"),
    OUT_OF_SERVICE("OUT_OF_SERVICE", "El servicio está fuera de servicio temporalmente");

    private final String value;
    private final String description;

    HealthStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value;
    }
}