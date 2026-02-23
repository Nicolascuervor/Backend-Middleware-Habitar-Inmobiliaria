package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HubSpotResponseDTO {
    // HubSpot devuelve los objetos dentro de un array llamado "results"
    private List<HubSpotContactDTO> results;

    // Para manejo de paginación (opcional por ahora, pero buena práctica)
    private Paging paging;

    @Data
    public static class Paging {
        private Next next;
    }

    @Data
    public static class Next {
        private String after;
        private String link;
    }
}
