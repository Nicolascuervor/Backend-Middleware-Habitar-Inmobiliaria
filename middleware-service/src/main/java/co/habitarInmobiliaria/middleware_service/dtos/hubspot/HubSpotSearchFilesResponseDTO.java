package co.habitarinmobiliaria.middleware_service.dtos.hubspot;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class HubSpotSearchFilesResponseDTO {
    private List<Map<String, Object>> results;
}
