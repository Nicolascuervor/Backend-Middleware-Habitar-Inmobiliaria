package co.habitarinmobiliaria.middleware_service.strategy.concrete_strategies;

import co.habitarinmobiliaria.middleware_service.strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class DescartarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-DESCARTADO"; }
    public String getNombre() { return "DESCARTADO"; }
    public boolean aplicaPara(String accion) { return "descartar".equalsIgnoreCase(accion); }
}