package com.driagon.services.logging.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class OperationContextInterceptor implements HandlerInterceptor {

    private static final String OPERATION_KEY = "OPERATION";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {

            if (MDC.get(OPERATION_KEY) != null) {
                MDC.remove(OPERATION_KEY);
            }

            String methodName = handlerMethod.getMethod().getName();
            MDC.put(OPERATION_KEY, methodName);
        }

        return true;
    }
}