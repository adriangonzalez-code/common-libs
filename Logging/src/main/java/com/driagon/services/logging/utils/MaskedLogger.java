package com.driagon.services.logging.utils;

import com.driagon.services.logging.annotations.Exclude;
import com.driagon.services.logging.annotations.Mask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MaskedLogger {

    private final Logger logger;
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    private MaskedLogger(Logger logger) {
        this.logger = logger;
    }

    public static MaskedLogger getLogger(Class<?> clazz) {
        return new MaskedLogger(LoggerFactory.getLogger(clazz));
    }

    public static MaskedLogger getLogger(String name) {
        return new MaskedLogger(LoggerFactory.getLogger(name));
    }

    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            Object[] maskedArgs = maskArgumentsWithMethodContext(args);
            logger.info(message, maskedArgs);
        }
    }

    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            Object[] maskedArgs = maskArgumentsWithMethodContext(args);
            logger.debug(message, maskedArgs);
        }
    }

    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            Object[] maskedArgs = maskArgumentsWithMethodContext(args);
            logger.warn(message, maskedArgs);
        }
    }

    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            Object[] maskedArgs = maskArgumentsWithMethodContext(args);
            logger.error(message, maskedArgs);
        }
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            Object[] maskedArgs = maskArgumentsWithMethodContext(args);
            logger.trace(message, maskedArgs);
        }
    }

    /**
     * Enmascara argumentos teniendo en cuenta el contexto del método que llama al logger
     */
    private Object[] maskArgumentsWithMethodContext(Object... args) {
        if (args == null || args.length == 0) return args;

        try {
            // Obtener información del método que llamó al logger
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement callerElement = findCallerMethod(stackTrace);

            if (callerElement != null) {
                Method callerMethod = getMethodFromStackTrace(callerElement);
                if (callerMethod != null) {
                    return maskArgumentsWithMethod(callerMethod, args);
                }
            }
        } catch (Exception e) {
            // Si hay algún error obteniendo el contexto, usar enmascaramiento básico
            logger.debug("Error obtaining method context for masking, using basic masking", e);
        }

        // Fallback: usar enmascaramiento básico
        return MaskingUtils.maskLoggingArguments(args);
    }

    /**
     * Encuentra el método que llamó al logger en el stack trace
     */
    private StackTraceElement findCallerMethod(StackTraceElement[] stackTrace) {
        boolean foundLogger = false;
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(MaskedLogger.class.getName())) {
                foundLogger = true;
                continue;
            }
            if (foundLogger && !element.getClassName().equals(MaskedLogger.class.getName())) {
                return element;
            }
        }
        return null;
    }

    /**
     * Obtiene el objeto Method a partir del stack trace element
     */
    private Method getMethodFromStackTrace(StackTraceElement element) {
        String cacheKey = element.getClassName() + "." + element.getMethodName();

        return METHOD_CACHE.computeIfAbsent(cacheKey, key -> {
            try {
                Class<?> callerClass = Class.forName(element.getClassName());
                // Buscar métodos que coincidan con el nombre
                Method[] methods = callerClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(element.getMethodName())) {
                        return method;
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.debug("Could not find class for stack trace element: {}", element.getClassName());
            }
            return null;
        });
    }

    /**
     * Enmascara argumentos usando información del método
     */
    private Object[] maskArgumentsWithMethod(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Object[] maskedArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            // Si tenemos información del parámetro correspondiente
            if (i < parameters.length) {
                Parameter param = parameters[i];

                if (param.isAnnotationPresent(Exclude.class)) {
                    maskedArgs[i] = "[EXCLUDED]";
                } else if (param.isAnnotationPresent(Mask.class)) {
                    maskedArgs[i] = MaskingUtils.maskField(arg, param.getAnnotation(Mask.class));
                } else {
                    maskedArgs[i] = MaskingUtils.maskSensitiveData(arg);
                }
            } else {
                // Si no hay parámetro correspondiente, usar enmascaramiento estándar
                maskedArgs[i] = MaskingUtils.maskSensitiveData(arg);
            }
        }

        return maskedArgs;
    }

    // Métodos de verificación de nivel
    public boolean isDebugEnabled() { return logger.isDebugEnabled(); }
    public boolean isInfoEnabled() { return logger.isInfoEnabled(); }
    public boolean isWarnEnabled() { return logger.isWarnEnabled(); }
    public boolean isErrorEnabled() { return logger.isErrorEnabled(); }
    public boolean isTraceEnabled() { return logger.isTraceEnabled(); }
}