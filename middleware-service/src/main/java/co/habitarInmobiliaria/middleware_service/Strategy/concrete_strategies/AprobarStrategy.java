package co.habitarinmobiliaria.middleware_service.Strategy.concrete_strategies;

import co.habitarinmobiliaria.middleware_service.Strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class AprobarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-APROBADO"; }
    public String getNombreEstado() { return "APROBADO"; }
    public boolean aplicaPara(String accion) { return "aprobar".equalsIgnoreCase(accion); }
}
