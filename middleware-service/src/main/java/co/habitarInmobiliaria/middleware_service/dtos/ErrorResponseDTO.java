package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Estructura estandarizada para reportar errores al Frontend")
public class ErrorResponseDTO {

    @Schema(description = "Código único para rastrear el error en los logs", example = "a1b2-c3d4-e5f6")
    private String traceId;

    @Schema(description = "Fecha y hora del incidente", example = "2026-02-12T10:15:30")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP", example = "404")
    private int status;

    @Schema(description = "Tipo de error técnico", example = "RecursoNoEncontradoException")
    private String error;

    @Schema(description = "Mensaje amigable para el desarrollador frontend (o usuario)", example = "No se encontró el contacto en HubSpot con el token proporcionado")
    private String mensaje;

    @Schema(description = "Ruta donde ocurrió el error", example = "/api/v1/vitrina/12345")
    private String path;
}
