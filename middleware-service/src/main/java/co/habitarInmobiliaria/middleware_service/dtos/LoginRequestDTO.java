package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record LoginRequestDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Formato de correo inválido")
        @Size(max = 254, message = "Correo demasiado largo")
        @Schema(example = "asesor@habitar.co")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "Contraseña entre 8 y 128 caracteres")
        String password
) {}
