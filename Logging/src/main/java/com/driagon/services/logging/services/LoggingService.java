package com.driagon.services.logging.services;

import com.driagon.services.logging.annotations.ExceptionLog;
import com.driagon.services.logging.utils.MaskingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;
import java.util.Optional;

public class LoggingService {

    public void logEntry(String methodName, Object[] args, Level level) {
        Logger logger = LoggerFactory.getLogger(methodName);
        String maskedArgs = MaskingUtils.maskSensitiveData(args);
        log(logger, level, "Entrada - Método: {} - Argumentos: {}", methodName, maskedArgs);
    }

    public void logExit(String methodName, Object result, long duration, Level level) {
        Logger logger = LoggerFactory.getLogger(methodName);
        String maskedResult = MaskingUtils.maskSensitiveData(result);
        log(logger, level, "Salida - Método: {} - Resultado: {} - Duración: {}ms",
                methodName, maskedResult, duration);
    }

    public void logException(String methodName, Throwable ex, ExceptionLog[] expectedExceptions, Level defaultLevel) {
        Logger logger = LoggerFactory.getLogger(methodName);
        String rootCause = getRootCause(ex);

        // Buscar si la excepción está en las esperadas
        Optional<ExceptionLog> exceptionConfig = Arrays.stream(expectedExceptions)
                .filter(exc -> exc.value().isInstance(ex))
                .findFirst();

        if (exceptionConfig.isPresent()) {
            ExceptionLog config = exceptionConfig.get();
            String message = config.message().isEmpty() ? rootCause : config.message();

            log(logger, defaultLevel, "[{}] Error controlado - Tipo: {} - Mensaje: {}",
                    methodName, ex.getClass().getSimpleName(), message);

            if (config.printStackTrace()) {
                logger.error("Stacktrace completo para error controlado:", ex);
            }
        } else {
            // Excepción no esperada
            log(logger, defaultLevel, "[{}] Error no controlado - Tipo: {} - Mensaje: {}",
                    methodName, ex.getClass().getSimpleName(), rootCause);
        }
    }

    private String getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return Optional.ofNullable(cause.getMessage())
                .filter(msg -> !msg.isBlank())
                .orElse("Sin mensaje de error");
    }

    private void log(Logger logger, Level level, String message, Object... args) {
        switch (level) {
            case DEBUG -> logger.debug(message, args);
            case INFO -> logger.info(message, args);
            case WARN -> logger.warn(message, args);
            case ERROR -> logger.error(message, args);
            default -> logger.error(message, args);
        }
    }
}