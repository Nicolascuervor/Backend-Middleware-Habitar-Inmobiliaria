package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VitrinaResponseDTO {
    private AsesorInfo asesor;
    private List<VitrinaInmuebleDTO> inmuebles;

    /**
     * Total de inmuebles que esta respuesta debe cubrir para el token: cantidad de listings
     * con URL válida en HubSpot (alquiler + venta). Sin paginación: en respuesta correcta
     * {@code totalInmuebles == inmuebles.size()}; si hay fallos parciales, el backend puede
     * responder 503 con {@code inmuebles.size() < totalInmuebles}.
     */
    @Schema(
            name = "totalInmuebles",
            description = "Total esperado de inmuebles en esta respuesta (listings con URL válida). "
                    + "Debe coincidir con inmuebles.length cuando la respuesta es completa (HTTP 200). "
                    + "Si hay datos faltantes por fallos externos, puede enviarse HTTP 503 con el mismo cuerpo.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalInmuebles;

    private List<String> alertas; // Mensajes de degradación parcial (ej: "Portal Wasi no disponible")

    @Data
    @Builder
    public static class AsesorInfo {
        private String nombreCompleto;
        private String correo;
        private String telefono;
        private String fotoUrl;
        private String linkMeeting;
    }
}
