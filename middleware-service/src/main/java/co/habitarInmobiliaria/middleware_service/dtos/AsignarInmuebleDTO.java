package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AsignarInmuebleDTO {

    @Schema(description = "URL completa del inmueble generada desde Wasi",
            example = "https://buscador.habitarinmobiliaria.co/.../9773703")
    private String urlWasi;
}
