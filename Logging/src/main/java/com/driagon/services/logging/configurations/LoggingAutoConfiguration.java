package com.driagon.services.logging.configurations;

import com.driagon.services.logging.aspects.LoggingAspect;
import com.driagon.services.logging.filters.RequestResponseLoggingFilter;
import com.driagon.services.logging.properties.FilterProperties;
import com.driagon.services.logging.services.LoggingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({FilterProperties.class})
@ConditionalOnProperty(prefix = "logging.aspect", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LoggingService loggingService() {
        return new LoggingService();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingAspect loggingAspect(LoggingService loggingService) {
        return new LoggingAspect(loggingService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "logging.filter", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public RequestResponseLoggingFilter loggingFilter(Environment environment, FilterProperties filterProperties) {
        return new RequestResponseLoggingFilter(
                environment,
                filterProperties.getExcludePaths(),
                filterProperties.getRequestHeaders(),
                filterProperties.getResponseHeaders()
        );
    }
}