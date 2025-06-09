package com.driagon.services.healthcheck.models;

import com.driagon.services.healthcheck.constants.HealthStatus;

import java.time.LocalDateTime;
import java.util.Map;

public class ServiceHealth {

    /**
     * Nombre del servicio o dependencia
     */
    private String serviceName;

    /**
     * Estado del servicio
     */
    private HealthStatus status;

    /**
     * Detalles adicionales del health check
     */
    private Map<String, Object> details;

    /**
     * Timestamp del último check
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Tiempo de respuesta en milisegundos
     */
    private Long responseTimeMs;

    /**
     * Mensaje descriptivo del estado
     */
    private String message;

    /**
     * URL o endpoint verificado (opcional)
     */
    private String url;

    /**
     * Query o comando ejecutado (opcional, para DB)
     */
    private String query;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}