package com.driagon.services.logging.aspects;

import com.driagon.services.logging.annotations.Loggable;
import com.driagon.services.logging.services.LoggingService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Optional;

@Aspect
public class LoggingAspect {

    private final LoggingService loggingService;

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("(@annotation(com.driagon.services.logging.annotations.Loggable) || @within(com.driagon.services.logging.annotations.Loggable)) && execution(* *(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Loggable loggable = getLoggableAnnotation(signature);
        String message = loggable.message();

        try {
            if (loggable.includeArgs()) {
                loggingService.logEntry(methodName, joinPoint.getArgs(), loggable.level(), message);
            }

            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (loggable.includeResult()) {
                loggingService.logExit(methodName, result, duration, loggable.level(), message);
            }

            return result;
        } catch (Exception ex) {
            if (loggable.logUnexpectedExceptions() || loggable.exceptions().length > 0) {
                loggingService.logException(
                        methodName,
                        ex,
                        loggable.exceptions(),
                        loggable.exceptionLevel()
                );
            }
            throw ex;
        }
    }

    private Loggable getLoggableAnnotation(MethodSignature methodSignature) {
        return Optional.ofNullable(methodSignature.getMethod().getAnnotation(Loggable.class))
                .orElse(methodSignature.getMethod().getDeclaringClass().getAnnotation(Loggable.class));
    }
}