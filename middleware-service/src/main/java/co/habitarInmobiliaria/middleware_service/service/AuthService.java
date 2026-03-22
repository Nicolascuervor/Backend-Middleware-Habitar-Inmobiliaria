package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.dtos.*;
import co.habitarinmobiliaria.middleware_service.dtos.airtable.AirtableFieldsDTO;
import co.habitarinmobiliaria.middleware_service.dtos.airtable.AirtableResponseDTO;
import co.habitarinmobiliaria.middleware_service.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AirtableClient airtableClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /* ID de la tabla de asesores*/
    @org.springframework.beans.factory.annotation.Value("${airtable.table.asesores}")
    private String tablaAsesores;

    /* Patrón de correo válido */
    private static final java.util.regex.Pattern PATRON_CORREO =
            java.util.regex.Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    public AuthResponseDTO login(LoginRequestDTO request) {
        /* Sanitizar input para logs */
        String correoSeguro = request.correo() != null
                ? request.correo().replaceAll("[\\r\\n\\t]", "_")
                : "";
        log.info("Intento de login para: {}", correoSeguro);

        /* Validar formato de correo */
        if (request.correo() == null || !PATRON_CORREO.matcher(request.correo()).matches()) {
            log.warn("Formato de correo inválido: {}", correoSeguro);
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        /* Escapar caracteres especiales para fórmula de Airtable */
        String correoEscapado = request.correo()
                .replace("\\", "\\\\")
                .replace("'", "\\'");

        /* Buscar asesor por correo en Airtable */
        String formula = "{Correo} = '" + correoEscapado + "'";

        /* Consultar Airtable */
        AirtableResponseDTO response = airtableClient.buscarRegistrosPorFormula(tablaAsesores, formula);

        /* Verificar si existe el usuario */
        if (response.records() == null || response.records().isEmpty()) {
            log.warn("Usuario no encontrado en Airtable: {}", correoSeguro);
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        AirtableFieldsDTO asesor = response.records().get(0).fields();

        /* Validar contraseña */
        if (!passwordEncoder.matches(request.password(), asesor.password())) {
            log.warn("Contraseña incorrecta para: {}", correoSeguro);
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        /* Generar token JWT */
        String token = jwtService.generarToken(asesor.correo(), asesor.hubspotOwnerId());

        return new AuthResponseDTO(token, asesor.nombre(), asesor.correo());
    }
}