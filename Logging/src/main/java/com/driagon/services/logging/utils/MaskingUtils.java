package com.driagon.services.logging.utils;

import com.driagon.services.logging.annotations.Exclude;
import com.driagon.services.logging.annotations.Mask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaskingUtils {

    private static final Logger log = LoggerFactory.getLogger(MaskingUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Enmascara datos sensibles en cualquier objeto
     *
     * @param data Objeto a enmascarar
     * @return String con los datos enmascarados
     */
    public static String maskSensitiveData(Object data) {
        if (data == null) {
            return "null";
        }

        if (data.getClass().isPrimitive() || data instanceof String || data instanceof Number || data instanceof Boolean) {
            return data.toString();
        }

        if (data instanceof Collection<?>) {
            return maskCollection((Collection<?>) data);
        }

        if (data.getClass().isArray()) {
            return maskArray(data);
        }

        if (data instanceof Map) {
            return maskMap((Map<?, ?>) data);
        }

        return maskObject(data);
    }

    /**
     * Enmascara una colección de objetos
     */
    private static String maskCollection(Collection<?> collection) {
        List<String> maskedItems = new ArrayList<>();
        for (Object item : collection) {
            maskedItems.add(maskSensitiveData(item));
        }
        return maskedItems.toString();
    }

    /**
     * Enmascara un array de objetos
     */
    private static String maskArray(Object array) {
        if (array instanceof Object[]) {
            return maskCollection(Arrays.asList((Object[]) array));
        }
        // Manejo de arrays primitivos
        if (array instanceof int[]) return Arrays.toString((int[]) array);
        if (array instanceof long[]) return Arrays.toString((long[]) array);
        if (array instanceof double[]) return Arrays.toString((double[]) array);
        if (array instanceof boolean[]) return Arrays.toString((boolean[]) array);
        if (array instanceof char[]) return Arrays.toString((char[]) array);
        if (array instanceof byte[]) return Arrays.toString((byte[]) array);
        if (array instanceof float[]) return Arrays.toString((float[]) array);
        if (array instanceof short[]) return Arrays.toString((short[]) array);

        return array.toString();
    }

    /**
     * Enmascara un Map
     */
    private static String maskMap(Map<?, ?> map) {
        Map<Object, Object> maskedMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            maskedMap.put(
                    maskSensitiveData(entry.getKey()),
                    maskSensitiveData(entry.getValue())
            );
        }
        return maskedMap.toString();
    }

    /**
     * Enmascara un objeto complejo analizando sus campos
     */
    private static String maskObject(Object obj) {
        try {
            Map<String, Object> maskedFields = new HashMap<>();
            Class<?> clazz = obj.getClass();

            // Recorrer todos los campos, incluyendo los heredados
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);

                    // Ignorar campos con @Exclude
                    if (field.isAnnotationPresent(Exclude.class)) {
                        continue;
                    }

                    Object value = field.get(obj);

                    // Si el campo tiene @Mask, aplicar el enmascaramiento
                    if (field.isAnnotationPresent(Mask.class) && value != null) {
                        maskedFields.put(field.getName(), maskField(value, field.getAnnotation(Mask.class)));
                    } else {
                        maskedFields.put(field.getName(), maskSensitiveData(value));
                    }
                }
                clazz = clazz.getSuperclass();
            }

            return objectMapper.writeValueAsString(maskedFields);
        } catch (IllegalAccessException | JsonProcessingException e) {
            log.error("Error al enmascarar objeto: {}", e.getMessage());
            return "[Error al enmascarar objeto]";
        }
    }

    /**
     * Enmascara un campo específico según la configuración de @Mask
     */
    private static String maskField(Object value, Mask maskAnnotation) {
        if (value == null) return null;
        String stringValue = value.toString();
        if (stringValue.isEmpty()) return stringValue;

        int visibleChars = maskAnnotation.visibleChars();
        char maskChar = maskAnnotation.maskChar();
        Mask.Position position = maskAnnotation.position();

        // Asegurar que visibleChars no sea mayor que la longitud del string
        visibleChars = Math.min(visibleChars, stringValue.length());

        StringBuilder maskedValue = new StringBuilder();
        int totalLength = stringValue.length();
        int maskedLength = totalLength - visibleChars;

        if (position == Mask.Position.SUFFIX) {
            // Mostrar los últimos 'visibleChars' caracteres
            maskedValue.append(repeat(maskChar, maskedLength));
            maskedValue.append(stringValue.substring(maskedLength));
        } else {
            // Mostrar los primeros 'visibleChars' caracteres
            maskedValue.append(stringValue.substring(0, visibleChars));
            maskedValue.append(repeat(maskChar, maskedLength));
        }

        return maskedValue.toString();
    }

    /**
     * Utilidad para repetir un carácter n veces
     */
    private static String repeat(char c, int times) {
        return String.valueOf(c).repeat(Math.max(0, times));
    }
}