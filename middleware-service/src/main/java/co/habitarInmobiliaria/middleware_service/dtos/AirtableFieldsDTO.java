package co.habitarinmobiliaria.middleware_service.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirtableFieldsDTO(
        @JsonProperty("Nombre") String nombre,
        @JsonProperty("Correo") String correo,
        @JsonProperty("Password") String password,
        @JsonProperty("HubspotOwnerId") String hubspotOwnerId
){}