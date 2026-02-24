package co.habitarinmobiliaria.middleware_service.dtos;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteAsesorDTO {
    private String idContacto;
    private String nombreCompleto;
    // Esto permite que el JSON tenga "listing_1": "url", "listing_2": "url" dinámicamente
    @JsonAnyGetter
    private Map<String, String> listings;

    @JsonAnyGetter
    public Map<String, String> getListings() {
        return listings;
    }
}
