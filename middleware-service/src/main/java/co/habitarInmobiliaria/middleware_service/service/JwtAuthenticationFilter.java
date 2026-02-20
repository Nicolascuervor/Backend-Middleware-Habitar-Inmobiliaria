package co.habitarinmobiliaria.middleware_service.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String correoAsesor;

        // Si no hay token o no empieza con "Bearer ", pasamos al siguiente filtro (rechazará la petición si era privada)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token (quitamos los primeros 7 caracteres: "Bearer ")
        jwt = authHeader.substring(7);
        try {
            correoAsesor = jwtService.extraerCorreo(jwt);

            // Si hay correo y el usuario no está autenticado aún en el contexto de Spring
            if (correoAsesor != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.esTokenValido(jwt, correoAsesor)) {
                    // Autorizamos explícitamente la petición
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            correoAsesor, null, new ArrayList<>() // Aquí irían los roles/autorizaciones si tuvieras
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si el token expira o es inválido, el contexto queda nulo y Spring Security lanzará 401
            logger.error("Error validando token JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
