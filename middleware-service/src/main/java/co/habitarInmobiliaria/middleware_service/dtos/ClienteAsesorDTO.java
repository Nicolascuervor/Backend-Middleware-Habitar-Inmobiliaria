package co.habitarinmobiliaria.middleware_service.dtos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteAsesorDTO {
    private String idContacto;
    private String nombreCompleto;
    /* Listings serializados dinámicamente en JSON */
    @JsonAnyGetter
    private Map<String, String> listings;

    @JsonAnyGetter
    public Map<String, String> getListings() {
        return listings;
    }
}
