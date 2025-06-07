package com.driagon.services.logging.services;

import com.driagon.services.logging.annotations.ExceptionLog;
import com.driagon.services.logging.annotations.Exclude;
import com.driagon.services.logging.annotations.Mask;
import com.driagon.services.logging.constants.Level;
import com.driagon.services.logging.constants.OperationTypeEnum;
import com.driagon.services.logging.utils.MaskingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import static com.driagon.services.logging.constants.LoggingMessages.ENTRY_FORMAT;
import static com.driagon.services.logging.constants.LoggingMessages.ERROR_CONTROLLED;
import static com.driagon.services.logging.constants.LoggingMessages.ERROR_CONTROLLED_STACKTRACE;
import static com.driagon.services.logging.constants.LoggingMessages.ERROR_UNCONTROLLED;
import static com.driagon.services.logging.constants.LoggingMessages.ERROR_UNCONTROLLED_STACKTRACE;
import static com.driagon.services.logging.constants.LoggingMessages.EXIT_FORMAT;
import static com.driagon.services.logging.constants.LoggingMessages.SIN_MENSAJE_ERROR;

public class LoggingService {

    public void logOperation(String className, String methodName, OperationTypeEnum type, Level level,
                             String message, Object... args) {
        Logger logger = LoggerFactory.getLogger(className); // ✅ Usar className para el logger
        String format = type == OperationTypeEnum.ENTRY ? ENTRY_FORMAT : EXIT_FORMAT;

        // Si es una operación de salida, el último argumento es la duración
        if (type == OperationTypeEnum.EXIT) {
            // Extraer la duración (último argumento)
            long duration = (long) args[args.length - 1];
            // Crear nuevo array sin la duración
            Object[] resultArgs = Arrays.copyOf(args, args.length - 1);

            // Enmascarar los argumentos (excepto la duración)
            Object[] maskedArgs = new Object[]{
                    message.isEmpty() ? "" : message,
                    methodName,
                    MaskingUtils.maskSensitiveData(resultArgs[0]),
                    duration
            };

            log(logger, level, format, maskedArgs);
        } else {
            // Para operación de entrada, procesar normalmente
            Object[] maskedArgs = new Object[]{
                    message.isEmpty() ? "" : message,
                    methodName,
                    MaskingUtils.maskSensitiveData(args)
            };

            log(logger, level, format, maskedArgs);
        }
    }

    public void logException(String className, String methodName, Method method, Object[] originalArgs, Throwable ex, ExceptionLog[] expectedExceptions, Level defaultLevel) {
        Logger logger = LoggerFactory.getLogger(className); // ✅ Usar className para el logger
        String rootCause = getRootCause(ex);

        Optional<ExceptionLog> exceptionConfig = Arrays.stream(expectedExceptions)
                .filter(exc -> exc.value().isInstance(ex))
                .findFirst();

        if (exceptionConfig.isPresent()) {
            ExceptionLog config = exceptionConfig.get();
            // Usar el nivel específico de la excepción configurada
            Level specificLevel = config.exceptionLevel();

            // Si hay un mensaje personalizado en la anotación, formatearlo con los argumentos enmascarados
            String message = config.message().isEmpty() ? getRootCause(ex) :
                    formatMessageWithArgs(config.message(), method, originalArgs);

            log(logger, specificLevel, ERROR_CONTROLLED,
                    methodName, ex.getClass().getSimpleName(), message);

            if (config.printStackTrace()) {
                logger.error(ERROR_CONTROLLED_STACKTRACE, ex);
            }
        } else {
            // Para excepciones inesperadas, usar el nivel por defecto
            log(logger, defaultLevel, ERROR_UNCONTROLLED, methodName, ex.getClass().getSimpleName(), rootCause);
            logger.error(ERROR_UNCONTROLLED_STACKTRACE, ex);
        }
    }

    private String getRootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return Optional.ofNullable(cause.getMessage())
                .filter(msg -> !msg.isBlank())
                .orElse(SIN_MENSAJE_ERROR);
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

    // Formatea el mensaje reemplazando {0}, {1}... por args[i]
    public String formatMessageWithArgs(String message, Method method, Object[] args) {
        if (message == null || message.isBlank() || args == null) return "";

        String result = message;
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < args.length; i++) {
            String placeholder = "{" + i + "}";
            if (!result.contains(placeholder)) continue;

            String value;
            if (i < parameters.length && parameters[i].isAnnotationPresent(Mask.class)) {
                // Si el parámetro tiene @Mask, aplicamos el enmascaramiento
                value = MaskingUtils.maskField(args[i], parameters[i].getAnnotation(Mask.class));
            } else if (i < parameters.length && parameters[i].isAnnotationPresent(Exclude.class)) {
                // Si el parámetro tiene @Exclude, lo ocultamos
                value = "[EXCLUDED]";
            } else {
                // Para cualquier otro caso (objetos, campos, etc.), aplicamos enmascaramiento inteligente
                value = MaskingUtils.maskSensitiveData(args[i]);
            }

            result = result.replace(placeholder, value);
        }

        return result;
    }
}