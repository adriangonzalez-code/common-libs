package com.driagon.services.logging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@ConfigurationProperties(prefix = "logging.filter")
public class FilterProperties {

    private final boolean enabled = true;
    private final List<String> excludePaths = Arrays.asList("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/favicon.ico", "/error", "/health", "/info", "/metrics", "/trace", "/loggers", "/heapdump", "/threaddump", "/auditevents", "/shutdown", "/env", "/configprops", "/beans", "/scheduledtasks", "/caches", "/conditions", "/mappings", "/httptrace");
    private final List<String> requestHeaders = Arrays.asList("Authorization", "Content-Type", "X-Request-ID", "X-Correlation-ID");
    private final List<String> responseHeaders = Arrays.asList("Content-Type", "Content-Length", "X-Request-ID", "X-Correlation-ID");

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public List<String> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getResponseHeaders() {
        return responseHeaders;
    }
}