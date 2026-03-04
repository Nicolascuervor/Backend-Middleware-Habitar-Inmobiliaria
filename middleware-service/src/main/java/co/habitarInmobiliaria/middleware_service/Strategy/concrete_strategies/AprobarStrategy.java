package co.habitarinmobiliaria.middleware_service.strategy.concrete_strategies;

import co.habitarinmobiliaria.middleware_service.strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class AprobarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-APROBADO"; }
    public String getNombreEstado() { return "APROBADO"; }
    public boolean aplicaPara(String accion) { return "aprobar".equalsIgnoreCase(accion); }
}
