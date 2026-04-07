package co.habitarinmobiliaria.middleware_service.dtos.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSpotFolderResponseDTO {
    private String id;
    private String name;
    private String parentFolderId;
}
