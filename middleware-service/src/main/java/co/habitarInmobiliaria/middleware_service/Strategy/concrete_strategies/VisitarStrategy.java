package co.habitarinmobiliaria.middleware_service.strategy.concrete_strategies;
import co.habitarinmobiliaria.middleware_service.strategy.EstadoInmuebleStrategy;
import org.springframework.stereotype.Component;

@Component
public class VisitarStrategy implements EstadoInmuebleStrategy {
    public String getSufijo() { return "-VISITADO"; }
    public String getNombre() { return "VISITADO"; }
    public boolean aplicaPara(String accion) { return "visitar".equalsIgnoreCase(accion); }
}
