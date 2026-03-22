package co.habitarinmobiliaria.middleware_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AsignarInmuebleDTO {

    @Schema(description = "URL completa del inmueble generada desde Wasi", example = "https://buscador.habitarinmobiliaria.co/.../9773703")
    private String url;

    @Schema(description = "Define si el espacio de destino en HubSpot es de VENTA o ALQUILER", example = "ALQUILER")
    @JsonProperty("tipo_inmueble")
    private String tipoInmueble;


}
