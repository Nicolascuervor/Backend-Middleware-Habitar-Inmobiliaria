package co.habitarinmobiliaria.middleware_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    /* Máximo de intentos por IP en la ventana de tiempo */
    private static final int MAX_INTENTOS = 10;
    private static final long VENTANA_MS = 60_000; /* 1 minuto */

    /* Mapa IP registro de intentos */
    private final ConcurrentHashMap<String, IntentoInfo> registroIntentos = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        /* Solo aplicar al endpoint de login */
        if (!"/api/v1/auth/login".equals(request.getRequestURI()) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = obtenerIpCliente(request);
        IntentoInfo info = registroIntentos.compute(ip, (key, actual) -> {
            long ahora = System.currentTimeMillis();
            if (actual == null || ahora - actual.inicioVentana > VENTANA_MS) {
                return new IntentoInfo(ahora, new AtomicInteger(1));
            }
            actual.contador.incrementAndGet();
            return actual;
        });

        if (info.contador.get() > MAX_INTENTOS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"mensaje\":\"Demasiados intentos. Espere 1 minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /* Obtener IP real del cliente */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /* Registro de intentos por IP */
    private static class IntentoInfo {
        final long inicioVentana;
        final AtomicInteger contador;

        IntentoInfo(long inicioVentana, AtomicInteger contador) {
            this.inicioVentana = inicioVentana;
            this.contador = contador;
        }
    }
}
