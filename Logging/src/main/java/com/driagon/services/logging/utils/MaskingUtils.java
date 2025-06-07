package com.driagon.services.logging.utils;

import com.driagon.services.logging.annotations.Exclude;
import com.driagon.services.logging.annotations.Mask;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class MaskingUtils {

    /**
     * Método helper para enmascarar manualmente un valor usando configuración por defecto
     * Útil para valores directos como request.getEmail()
     */
    public static String maskValue(Object value) {
        return maskValue(value, 4, '*', Mask.Position.SUFFIX);
    }

    /**
     * Método helper para enmascarar manualmente un valor con configuración personalizada
     */
    public static String maskValue(Object value, int visibleChars, char maskChar, Mask.Position position) {
        if (value == null) return "null";
        String stringValue = String.valueOf(value);
        if (stringValue.isEmpty()) return stringValue;

        return applyMask(stringValue, visibleChars, maskChar, position);
    }


    /**
     * Versión específica para argumentos de logging manual
     * Aplica enmascaramiento solo basado en anotaciones
     */
    public static Object[] maskLoggingArguments(Object... args) {
        if (args == null) return null;

        Object[] maskedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            maskedArgs[i] = maskSensitiveData(args[i]);
        }
        return maskedArgs;
    }

    /**
     * Método mejorado que verifica anotaciones en campos de objetos
     * NO aplica heurísticas automáticas, solo respeta anotaciones explícitas
     */
    public static String maskSensitiveData(Object obj) {
        if (obj == null) return "null";

        // Si es un tipo simple, devolverlo como string sin modificaciones
        if (isSimpleType(obj.getClass())) {
            return String.valueOf(obj);
        }

        // Si es un array, procesar cada elemento
        if (obj.getClass().isArray()) {
            return maskArray(obj);
        }

        // Si es una colección, procesar cada elemento
        if (obj instanceof Collection) {
            return maskCollection((Collection<?>) obj);
        }

        // Si es un Map, procesar llaves y valores
        if (obj instanceof Map) {
            return maskMap((Map<?, ?>) obj);
        }

        // Para objetos complejos, aplicar enmascaramiento a campos
        return maskObjectFields(obj);
    }

    /**
     * Enmascara campos de un objeto respetando SOLO anotaciones @Mask y @Exclude
     */
    private static String maskObjectFields(Object obj) {
        if (obj == null) return "null";

        Class<?> clazz = obj.getClass();
        StringBuilder result = new StringBuilder();
        result.append(clazz.getSimpleName()).append("{");

        Field[] fields = clazz.getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            // Saltar campos estáticos y sintéticos
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            if (!first) {
                result.append(", ");
            }
            first = false;

            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                result.append(field.getName()).append("=");

                // SOLO verificar anotaciones explícitas
                if (field.isAnnotationPresent(Exclude.class)) {
                    result.append("[EXCLUDED]");
                } else if (field.isAnnotationPresent(Mask.class)) {
                    Mask maskAnnotation = field.getAnnotation(Mask.class);
                    result.append(maskField(fieldValue, maskAnnotation));
                } else {
                    // Sin anotaciones = mostrar valor normal (recursivo para objetos complejos)
                    result.append(maskSensitiveData(fieldValue));
                }
            } catch (IllegalAccessException e) {
                result.append("[INACCESSIBLE]");
            }
        }

        result.append("}");
        return result.toString();
    }

    /**
     * Enmascara arrays
     */
    private static String maskArray(Object array) {
        StringBuilder result = new StringBuilder("[");
        int length = Array.getLength(array);

        for (int i = 0; i < length; i++) {
            if (i > 0) result.append(", ");
            Object element = Array.get(array, i);
            result.append(maskSensitiveData(element));
        }

        result.append("]");
        return result.toString();
    }

    /**
     * Enmascara colecciones
     */
    private static String maskCollection(Collection<?> collection) {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;

        for (Object element : collection) {
            if (!first) result.append(", ");
            first = false;
            result.append(maskSensitiveData(element));
        }

        result.append("]");
        return result.toString();
    }

    /**
     * Enmascara mapas
     */
    private static String maskMap(Map<?, ?> map) {
        StringBuilder result = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) result.append(", ");
            first = false;
            result.append(maskSensitiveData(entry.getKey()));
            result.append("=");
            result.append(maskSensitiveData(entry.getValue()));
        }

        result.append("}");
        return result.toString();
    }

    /**
     * Verifica si es un tipo simple
     */
    private static boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || clazz == Date.class
                || clazz == LocalDate.class
                || clazz == LocalDateTime.class
                || clazz == LocalTime.class
                || clazz.isEnum();
    }

    /**
     * Enmascara un campo específico según la configuración de @Mask
     */
    public static String maskField(Object value, Mask maskAnnotation) {
        if (value == null || maskAnnotation == null) return null;
        String stringValue = String.valueOf(value);
        if (stringValue.isEmpty()) return stringValue;

        return applyMask(
                stringValue,
                maskAnnotation.visibleChars(),
                maskAnnotation.maskChar(),
                maskAnnotation.position()
        );

    }

    private static String applyMask(String value, int visibleChars, char maskChar, Mask.Position position) {
        int totalLength = value.length();

        // Si el valor es más corto o igual que visibleChars, enmascarar todo
        if (totalLength <= visibleChars) {
            return repeat(maskChar, totalLength);
        }

        // Si el valor es más largo que visibleChars, aplicar la lógica de posición
        if (position == Mask.Position.SUFFIX) {
            int maskedLength = totalLength - visibleChars;
            return maskSuffix(value, maskedLength, maskChar);
        } else {
            return maskPrefix(value, visibleChars, maskChar);
        }

    }

    private static String maskSuffix(String value, int maskedLength, char maskChar) {
        return repeat(maskChar, maskedLength) + value.substring(maskedLength);
    }

    private static String maskPrefix(String value, int visibleChars, char maskChar) {
        return value.substring(0, visibleChars) + repeat(maskChar, value.length() - visibleChars);
    }

    public static Object[] processArguments(Method method, Object[] args) {
        if (method == null || args == null) return args;

        Parameter[] parameters = method.getParameters();
        Object[] result = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            if (parameters[i].isAnnotationPresent(Exclude.class)) {
                result[i] = "[EXCLUDED]";
            } else if (parameters[i].isAnnotationPresent(Mask.class)) {
                result[i] = maskField(args[i], parameters[i].getAnnotation(Mask.class));
            } else {
                result[i] = maskSensitiveData(args[i]);
            }
        }
        return result;
    }

    /**
     * Utilidad para repetir un carácter n veces
     */
    private static String repeat(char c, int times) {
        return String.valueOf(c).repeat(Math.max(0, times));
    }

    /**
     * Intenta detectar si un string podría contener información sensible
     * basándose en patrones comunes
     */
    private static String maskIfSensitiveString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }

        String lowerValue = value.toLowerCase();

        // Detectar patrones de contraseña (strings largos sin espacios)
        if (value.length() > 6 && !value.contains(" ") &&
                (lowerValue.contains("pass") || lowerValue.contains("secret") ||
                        lowerValue.contains("token") || lowerValue.contains("key"))) {
            return applyMask(value, 0, '*', Mask.Position.PREFIX);
        }

        // Si no detectamos patrones sensibles, devolver tal como está
        return value;
    }

}