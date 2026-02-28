package co.habitarinmobiliaria.middleware_service.Strategy.concrete_strategies;

import co.habitarinmobiliaria.middleware_service.Strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class VisitarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-VISITADO"; }
    public String getNombreEstado() { return "VISITADO"; }
    public boolean aplicaPara(String accion) { return "visitar".equalsIgnoreCase(accion); }
}
