package com.driagon.services.healthcheck.models;

import com.driagon.services.healthcheck.constants.HealthStatus;

import java.time.LocalDateTime;
import java.util.List;

public class HealthCheckResponse {

    /**
     * Estado general de la aplicación
     */
    private HealthStatus overallStatus;

    /**
     * Nombre de la aplicación
     */
    private String applicationName;

    /**
     * Versión de la aplicación
     */
    private String version;

    /**
     * Lista de servicios verificados
     */
    private List<ServiceHealth> services;

    /**
     * Timestamp de la verificación
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Tiempo total de verificación en milisegundos
     */
    private Long totalResponseTimeMs;

    /**
     * Información adicional del entorno
     */
    private String environment;

    public HealthStatus getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(HealthStatus overallStatus) {
        this.overallStatus = overallStatus;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ServiceHealth> getServices() {
        return services;
    }

    public void setServices(List<ServiceHealth> services) {
        this.services = services;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTotalResponseTimeMs() {
        return totalResponseTimeMs;
    }

    public void setTotalResponseTimeMs(Long totalResponseTimeMs) {
        this.totalResponseTimeMs = totalResponseTimeMs;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}