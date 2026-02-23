package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Data;
import java.util.List;

@Data
public class HubSpotSearchResponseDTO {
    private int total;
    private List<HubSpotContactDTO> results;
}
