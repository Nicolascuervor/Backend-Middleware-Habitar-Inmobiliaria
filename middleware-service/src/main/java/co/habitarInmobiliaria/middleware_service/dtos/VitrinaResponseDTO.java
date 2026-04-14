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
            description = "Total de inmuebles efectivamente retornados en el array inmuebles. "
                    + "Antes de responder, el backend valida este total contra el conteo esperado de HubSpot "
                    + "(listings_alquiler_filled_count + listings_venta_filled_count).",
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
