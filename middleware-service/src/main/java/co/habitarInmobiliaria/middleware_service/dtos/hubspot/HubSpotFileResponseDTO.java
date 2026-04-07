package co.habitarinmobiliaria.middleware_service.dtos.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSpotFileResponseDTO {
    private String id;
    private String url;
    private String name;
    private String folderId;
}
