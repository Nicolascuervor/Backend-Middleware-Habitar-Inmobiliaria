package co.habitarinmobiliaria.middleware_service.util;

/* Utilidad para sanitizar entradas en logs */
public final class LogSanitizer {
    private LogSanitizer() {}
    /* Reemplaza caracteres de control que permiten log injection */
    public static String sanitizar(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_");
    }
}
