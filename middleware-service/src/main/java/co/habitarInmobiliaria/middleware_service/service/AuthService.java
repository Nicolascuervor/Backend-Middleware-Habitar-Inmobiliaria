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

    // Usamos el Table ID proporcionado por la documentación oficial de Airtable
    private static final String TABLA_ASESORES = "tblLV7ZSUuV4Gu7Hv";

    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Intento de login para: {}", request.correo());

        // 1. Construimos la fórmula de Airtable para buscar por correo
        // Sintaxis de Airtable: {NombreColumna} = 'valor'
        String formula = "{Correo} = '" + request.correo() + "'";

        // 2. Consultamos Airtable
        AirtableResponseDTO response = airtableClient.buscarRegistrosPorFormula(TABLA_ASESORES, formula);

        // 3. Verificamos si existe el usuario
        if (response.records() == null || response.records().isEmpty()) {
            log.warn("Usuario no encontrado en Airtable: {}", request.correo());
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        // Extraemos los campos del primer registro encontrado
        AirtableFieldsDTO asesor = response.records().get(0).fields();

        // 4. Validamos la contraseña
        // NOTA DE INGENIERÍA: Las contraseñas en Airtable DEBEN estar encriptadas con BCrypt.
        // Si en tu Airtable tienes las contraseñas en texto plano por ahora, usa .equals() temporalmente,
        // pero lo correcto es usar passwordEncoder.matches()

        if (!passwordEncoder.matches(request.password(), asesor.password())) {
            log.warn("Contraseña incorrecta para: {}", request.correo());
            // Lanzamos el mismo error genérico por seguridad (para no dar pistas a atacantes)
            throw new RecursoNoEncontradoException("Credenciales inválidas");
        }

        // 5. Generamos el Token JWT
        String token = jwtService.generarToken(asesor.correo(), asesor.hubspotOwnerId());

        return new AuthResponseDTO(token, asesor.nombre(), asesor.correo());
    }
}