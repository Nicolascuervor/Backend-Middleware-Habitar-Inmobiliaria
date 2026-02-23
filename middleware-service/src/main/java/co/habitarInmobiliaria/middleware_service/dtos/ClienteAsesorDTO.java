package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteAsesorDTO {
    // Este ID es vital, es el UUID/Token que usaremos para abrir la vitrina de este abuelo
    private String idContacto;
    private String nombreCompleto;
    // Podrías agregar teléfono, correo o la cantidad de inmuebles asignados a futuro
}
