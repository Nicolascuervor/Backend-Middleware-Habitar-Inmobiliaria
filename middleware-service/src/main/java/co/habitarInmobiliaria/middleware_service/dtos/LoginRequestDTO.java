package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequestDTO(
        @Schema(example = "asesor_n@habitar.co") String correo,
        @Schema(example = "mi_contraseña_secreta") String password
) {}
