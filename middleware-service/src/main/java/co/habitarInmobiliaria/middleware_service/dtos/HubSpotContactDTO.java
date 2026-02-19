package co.habitarinmobiliaria.middleware_service.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HubSpotContactDTO {

    private String id;
    private PropertiesDTO properties;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertiesDTO {

        private String firstname;
        private String lastname;

        @JsonProperty("hs_avatar_filemanager_key")
        private String avatarKey;

        @JsonProperty("hubspot_owner_id")
        private String ownerId;

        // Mapeamos los 5 espacios de la vitrina
        // NOTA: Usamos el nombre exacto del JSON 'listing_1'
        @JsonAlias("listing_1")
        private String listing1;

        @JsonAlias("listing_2")
        private String listing2;

        @JsonAlias("listing_3")
        private String listing3;

        @JsonAlias("listing_4")
        private String listing4;

        @JsonAlias("listing_5")
        private String listing5;

        // TODO: Si deciden implementar la lógica de descartes, agreguen aquí el campo:
        // @JsonAlias("inmuebles_descartados")
        // private String inmueblesDescartados;
    }
}
