package com.driagon.services.healthcheck.services;

import com.driagon.services.healthcheck.constants.HealthStatus;
import com.driagon.services.healthcheck.indicators.BaseHealthIndicator;
import com.driagon.services.healthcheck.models.HealthCheckResponse;
import com.driagon.services.healthcheck.models.ServiceHealth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class HealthCheckService {

    @Value("${spring.application.name:Unknown Application}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${spring.profiles.active:default}")
    private String environment;

    private final List<BaseHealthIndicator> healthIndicators;

    @Autowired
    public HealthCheckService(List<BaseHealthIndicator> healthIndicators) {
        this.healthIndicators = healthIndicators;
    }

    /**
     * Ejecuta todos los health checks y retorna una respuesta consolidada
     */
    public HealthCheckResponse performHealthCheck() {
        long startTime = System.currentTimeMillis();

        List<ServiceHealth> serviceHealths = healthIndicators.parallelStream()
                .map(this::executeHealthCheck)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;
        HealthStatus overallStatus = determineOverallStatus(serviceHealths);

        HealthCheckResponse response = new HealthCheckResponse();
        response.setApplicationName(applicationName);
        response.setVersion(applicationVersion);
        response.setEnvironment(environment);
        response.setOverallStatus(overallStatus);
        response.setServices(serviceHealths);
        response.setTotalResponseTimeMs(totalTime);
        return response;
    }

    private ServiceHealth executeHealthCheck(BaseHealthIndicator indicator) {
        try {
            // Ejecutar con timeout
            CompletableFuture<Health> future = CompletableFuture.supplyAsync(indicator::health);
            Health health = future.get(30, TimeUnit.SECONDS);

            return convertHealthToServiceHealth(health);

        } catch (Exception e) {
            ServiceHealth serviceHealth = new ServiceHealth();
            serviceHealth.setServiceName(indicator.getClass().getSimpleName());
            serviceHealth.setStatus(HealthStatus.DOWN);
            serviceHealth.setMessage("Timeout o error: " + e.getMessage());
            return serviceHealth;
        }
    }

    private ServiceHealth convertHealthToServiceHealth(Health health) {
        HealthStatus status = switch (health.getStatus().getCode()) {
            case "UP" -> HealthStatus.UP;
            case "DOWN" -> HealthStatus.DOWN;
            case "OUT_OF_SERVICE" -> HealthStatus.OUT_OF_SERVICE;
            default -> HealthStatus.UNKNOWN;
        };

        ServiceHealth serviceHealth = new ServiceHealth();
        serviceHealth.setServiceName((String) health.getDetails().get("serviceName"));
        serviceHealth.setStatus(status);
        serviceHealth.setMessage((String) health.getDetails().get("message"));
        serviceHealth.setUrl((String) health.getDetails().get("url"));
        serviceHealth.setQuery((String) health.getDetails().get("query"));
        serviceHealth.setResponseTimeMs((Long) health.getDetails().get("responseTimeMs"));
        serviceHealth.setDetails(health.getDetails());
        return serviceHealth;
    }

    private HealthStatus determineOverallStatus(List<ServiceHealth> serviceHealths) {
        if (serviceHealths.isEmpty()) {
            return HealthStatus.UNKNOWN;
        }

        boolean hasDown = serviceHealths.stream()
                .anyMatch(s -> s.getStatus() == HealthStatus.DOWN);

        if (hasDown) {
            return HealthStatus.DOWN;
        }

        boolean allUp = serviceHealths.stream()
                .allMatch(s -> s.getStatus() == HealthStatus.UP);

        return allUp ? HealthStatus.UP : HealthStatus.UNKNOWN;
    }
}