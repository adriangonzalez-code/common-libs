package com.driagon.services.logging.aspects;

import com.driagon.services.logging.annotations.Loggable;
import com.driagon.services.logging.constants.OperationTypeEnum;
import com.driagon.services.logging.services.LoggingService;
import com.driagon.services.logging.utils.MaskingUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
public class LoggingAspect {

    private final LoggingService loggingService;
    private static final Map<Method, Loggable> LOGGABLE_CACHE = new ConcurrentHashMap<>();

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("(@annotation(com.driagon.services.logging.annotations.Loggable) || @within(com.driagon.services.logging.annotations.Loggable)) && execution(* *(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = getLoggableAnnotation(signature);
        if (loggable == null) return joinPoint.proceed();

        String methodName = signature.getName();
        String className = signature.getDeclaringType().getName(); // ✅ Obtener nombre completo de la clase
        Object[] originalArgs = joinPoint.getArgs();

        // Formateamos el mensaje respetando las anotaciones de los parámetros
        String message = loggingService.formatMessageWithArgs(loggable.message(), method, originalArgs);

        // Obtenemos los argumentos enmascarados para el log detallado
        Object[] maskedArgs = MaskingUtils.processArguments(method, originalArgs);

        try {
            // Log entrada con argumentos enmascarados
            if (loggable.includeArgs()) {
                loggingService.logOperation(className, methodName, OperationTypeEnum.ENTRY,
                        loggable.level(), message, maskedArgs);
            }

            // Ejecutar método con argumentos originales
            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Log salida
            if (loggable.includeResult()) {
                loggingService.logOperation(className, methodName, OperationTypeEnum.EXIT,
                        loggable.level(), message, result, duration);
            }

            return result;
        } catch (Exception ex) {
            if (loggable.logUnexpectedExceptions() || loggable.exceptions().length > 0) {
                loggingService.logException(
                        className,
                        methodName,
                        method,
                        originalArgs,
                        ex,
                        loggable.exceptions(),
                        loggable.unexpectedExceptionLevel()
                );
            }
            throw ex;
        }
    }

    private Loggable getLoggableAnnotation(MethodSignature methodSignature) {
        Method method = methodSignature.getMethod();
        return LOGGABLE_CACHE.computeIfAbsent(method, m -> {
            Loggable methodAnnotation = m.getAnnotation(Loggable.class);
            if (methodAnnotation != null) return methodAnnotation;
            return m.getDeclaringClass().getAnnotation(Loggable.class);
        });
    }
}