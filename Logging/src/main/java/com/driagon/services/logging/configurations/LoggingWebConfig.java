package com.driagon.services.logging.configurations;

import com.driagon.services.logging.filters.OperationContextInterceptor;
import com.driagon.services.logging.properties.FilterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoggingWebConfig implements WebMvcConfigurer {

    @Autowired
    private OperationContextInterceptor operationContextInterceptor;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(this.filterProperties.getExcludePaths());
    }
}