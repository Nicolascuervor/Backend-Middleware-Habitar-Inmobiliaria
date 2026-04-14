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

    @Schema(
            name = "totalInmuebles",
            description = "Total esperado de inmuebles en esta respuesta (listings con URL válida). "
                    + "Debe coincidir con inmuebles.length cuando la respuesta es completa (HTTP 200). "
                    + "Si hay datos faltantes por fallos externos, puede enviarse HTTP 503 con el mismo cuerpo.",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalInmuebles;

    private List<String> alertas;

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
