package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class VitrinaResponseDTO {
    private AsesorInfo asesor;
    private List<VitrinaInmuebleDTO> inmuebles;
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
