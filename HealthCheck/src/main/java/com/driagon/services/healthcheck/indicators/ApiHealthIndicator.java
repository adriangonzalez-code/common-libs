package com.driagon.services.healthcheck.indicators;

import com.driagon.services.healthcheck.constants.HealthStatus;
import com.driagon.services.healthcheck.models.ServiceHealth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("apiHealthIndicator")
@ConditionalOnProperty(
        prefix = "health-check",
        name = "api.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ApiHealthIndicator extends BaseHealthIndicator implements HealthIndicator {

    @Value("${spring.application.name:Unknown Application}")
    private String applicationName;

    @Value("${health-check.api.description:API REST}")
    private String apiDescription;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${management.server.port:${server.port}}")
    private String managementPort;

    @Override
    protected ServiceHealth doHealthCheck() {
        ServiceHealth serviceHealth = new ServiceHealth();
        serviceHealth.setServiceName(getServiceName());
        serviceHealth.setStatus(HealthStatus.UP);
        serviceHealth.setMessage("API funcionando correctamente");
        serviceHealth.setUrl("http://localhost:" + serverPort);
        serviceHealth.setDetails(Map.of(
                "application.name", applicationName,
                "description", apiDescription
        ));

        return serviceHealth;
    }

    @Override
    protected String getServiceName() {
        return applicationName + " - API";
    }
}