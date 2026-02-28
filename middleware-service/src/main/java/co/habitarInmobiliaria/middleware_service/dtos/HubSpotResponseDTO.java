package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HubSpotResponseDTO {
    /* Resultados de HubSpot */
    private List<HubSpotContactDTO> results;

    /* Paginación */
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
