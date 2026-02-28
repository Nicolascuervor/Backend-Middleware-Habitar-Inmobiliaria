package co.habitarinmobiliaria.middleware_service.Strategy.concrete_strategies;

import co.habitarinmobiliaria.middleware_service.Strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class DescartarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-DESCARTADO"; }
    public String getNombreEstado() { return "DESCARTADO"; }
    public boolean aplicaPara(String accion) { return "descartar".equalsIgnoreCase(accion); }
}