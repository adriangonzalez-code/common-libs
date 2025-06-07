package com.driagon.services.logging.constants;

public final class LoggingMessages {
    public static final String ENTRY_FORMAT = "{} - Entrada - Método: {} - Argumentos: {}";
    public static final String EXIT_FORMAT = "{} - Salida - Método: {} - Resultado: {} - Duración: {}ms";
    public static final String ERROR_CONTROLLED = "[{}] Error controlado - Tipo: {} - Mensaje: {}";
    public static final String ERROR_UNCONTROLLED = "[{}] Error no controlado - Tipo: {} - Mensaje: {}";
    public static final String ERROR_CONTROLLED_STACKTRACE = "Stacktrace completo para error controlado:";
    public static final String ERROR_UNCONTROLLED_STACKTRACE = "Stacktrace completo para error NO controlado:";
    public static final String SIN_MENSAJE_ERROR = "Sin mensaje de error";
    public static final String UNKNOWN = "unknown";
    public static final String METODO_ARGS = "Método: {} - Args: {}";
}