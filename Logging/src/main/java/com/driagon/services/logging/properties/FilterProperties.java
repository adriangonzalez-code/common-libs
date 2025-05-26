package com.driagon.services.logging.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;

@Getter
@Setter
@ConfigurationProperties(prefix = "logging.filter")
public class FilterProperties {

    private boolean enabled = true;
    private Collection<String> excludePaths;
    private Collection<String> requestHeaders;
    private Collection<String> responseHeaders;
}