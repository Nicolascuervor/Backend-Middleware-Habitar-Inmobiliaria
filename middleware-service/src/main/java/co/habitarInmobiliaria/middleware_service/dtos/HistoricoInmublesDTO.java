package co.habitarinmobiliaria.middleware_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoInmublesDTO {

    @NotBlank(message = "El código numérico del inmueble es obligatorio")
    private String codigoNumerico;

    @NotNull(message = "El cliente asociado es obligatorio")
    private Long clienteAsociado;

    @NotBlank(message = "El estado del código es obligatorio")
    private String estadoCodigo;
}
