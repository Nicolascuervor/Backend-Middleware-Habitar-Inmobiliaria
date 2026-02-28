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

        @JsonProperty("listing_1")
        private String listing1;

        @JsonProperty("listing_2")
        private String listing2;

        @JsonProperty("listing_3")
        private String listing3;

        @JsonProperty("listing_4")
        private String listing4;

        @JsonProperty("listing_5")
        private String listing5;

        @com.fasterxml.jackson.annotation.JsonIgnore
        private java.util.Map<String, String> propiedadesDinamicas = new java.util.HashMap<>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void setPropiedadDinamica(String key, String value) {
            propiedadesDinamicas.put(key, value);

        }
    }
}
