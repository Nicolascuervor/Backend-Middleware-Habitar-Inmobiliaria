package co.habitarinmobiliaria.middleware_service.Strategy;

public interface EstadoInmuebleStrategy {
    String getSufijo();

    String getNombreEstado();

    /* Retorna true si esta estrategia aplica para la acción dada */
    boolean aplicaPara(String accion);
}
