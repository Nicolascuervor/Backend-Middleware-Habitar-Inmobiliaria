package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClientesPaginadosDTO {
    private List<ClienteAsesorDTO> clientes;
    private String nextToken;
    private int totalClientes;
}
