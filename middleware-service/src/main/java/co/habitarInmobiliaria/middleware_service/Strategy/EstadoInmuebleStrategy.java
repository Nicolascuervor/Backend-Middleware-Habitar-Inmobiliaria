package co.habitarinmobiliaria.middleware_service.strategy;

public interface EstadoInmuebleStrategy {
    String getSufijo();
    String getNombre();
    /* Retorna true si esta estrategia aplica para la acción dada */
    boolean aplicaPara(String accion);
}
