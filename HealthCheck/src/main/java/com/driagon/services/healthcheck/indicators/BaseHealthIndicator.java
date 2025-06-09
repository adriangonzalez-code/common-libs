package com.driagon.services.healthcheck.indicators;

import com.driagon.services.healthcheck.constants.HealthStatus;
import com.driagon.services.healthcheck.models.ServiceHealth;
import com.driagon.services.logging.utils.MaskedLogger;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseHealthIndicator implements HealthIndicator {

    protected final MaskedLogger logger = MaskedLogger.getLogger(this.getClass());

    @Override
    public final Health health() {
        long startTime = System.currentTimeMillis();

        try {
            ServiceHealth serviceHealth = doHealthCheck();
            long responseTime = System.currentTimeMillis() - startTime;

            return buildHealthResponse(serviceHealth, responseTime);

        } catch (Exception e) {
            logger.error("Error ejecutando health check para {}: {}", getServiceName(), e.getMessage(), e);

            long responseTime = System.currentTimeMillis() - startTime;
            ServiceHealth errorHealth = new ServiceHealth();
            errorHealth.setServiceName(getServiceName());
            errorHealth.setStatus(HealthStatus.DOWN);
            errorHealth.setMessage("Error: " + e.getMessage());
            errorHealth.setResponseTimeMs(responseTime);

            return buildHealthResponse(errorHealth, responseTime);
        }
    }

    /**
     * Implementación específica del health check
     * @return ServiceHealth con el resultado del check
     */
    protected abstract ServiceHealth doHealthCheck();

    /**
     * Nombre del servicio que se está verificando
     * @return Nombre del servicio
     */
    protected abstract String getServiceName();

    /**
     * Construye la respuesta de Spring Boot Actuator basada en ServiceHealth
     */
    private Health buildHealthResponse(ServiceHealth serviceHealth, long responseTime) {
        // Convertir nuestro HealthStatus a Spring Boot Status
        Status springStatus = convertToSpringStatus(serviceHealth.getStatus());

        // Crear builder con el status correcto
        Health.Builder builder = Health.status(springStatus);

        // Agregar detalles de manera segura
        Map<String, Object> details = new HashMap<>();

        if (serviceHealth.getServiceName() != null) {
            details.put("serviceName", serviceHealth.getServiceName());
        }

        if (serviceHealth.getStatus() != null) {
            details.put("status", serviceHealth.getStatus().getValue());
        }

        if (serviceHealth.getMessage() != null) {
            details.put("message", serviceHealth.getMessage());
        }

        details.put("responseTimeMs", responseTime);

        if (serviceHealth.getTimestamp() != null) {
            details.put("timestamp", serviceHealth.getTimestamp());
        }

        if (serviceHealth.getUrl() != null) {
            details.put("url", serviceHealth.getUrl());
        }

        if (serviceHealth.getQuery() != null) {
            details.put("query", serviceHealth.getQuery());
        }

        // Agregar detalles adicionales si existen
        if (serviceHealth.getDetails() != null) {
            details.putAll(serviceHealth.getDetails());
        }

        return builder.withDetails(details).build();
    }

    /**
     * Convierte nuestro HealthStatus a Spring Boot Status
     */
    private Status convertToSpringStatus(HealthStatus healthStatus) {
        if (healthStatus == null) {
            return Status.UNKNOWN;
        }

        return switch (healthStatus) {
            case UP -> Status.UP;
            case DOWN -> Status.DOWN;
            case OUT_OF_SERVICE -> Status.OUT_OF_SERVICE;
            case UNKNOWN -> Status.UNKNOWN;
        };
    }
}