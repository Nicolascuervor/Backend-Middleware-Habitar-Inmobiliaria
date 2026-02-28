package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.dtos.*;
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

    /* ID de la tabla de asesores en Airtable */
    private static final String TABLA_ASESORES = "tblLV7ZSUuV4Gu7Hv";

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Intento de login para: {}", request.correo());

        /* Buscar asesor por correo en Airtable */
        String formula = "{Correo} = '" + request.correo() + "'";

        /* Consultar Airtable */
        AirtableResponseDTO response = airtableClient.buscarRegistrosPorFormula(TABLA_ASESORES, formula);

        /* Verificar si existe el usuario */
        if (response.records() == null || response.records().isEmpty()) {
            log.warn("Usuario no encontrado en Airtable: {}", request.correo());
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        AirtableFieldsDTO asesor = response.records().get(0).fields();

        /* Validar contraseña */
        if (!passwordEncoder.matches(request.password(), asesor.password())) {
            log.warn("Contraseña incorrecta para: {}", request.correo());
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        /* Generar token JWT */
        String token = jwtService.generarToken(asesor.correo(), asesor.hubspotOwnerId());

        return new AuthResponseDTO(token, asesor.nombre(), asesor.correo());
    }
}