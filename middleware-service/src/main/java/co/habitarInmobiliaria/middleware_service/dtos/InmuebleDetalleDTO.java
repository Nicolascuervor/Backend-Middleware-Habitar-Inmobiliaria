package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InmuebleDetalleDTO {
    /* Cabecera y precios */
    private String titulo;
    private String tipoNegocio;
    private String precioFormateado;
    private String valorAdministracion;
    private String estadoActualCliente;

    /* Ubicación */
    private String ubicacion;
    private String zona;
    private String direccion;
    private String estrato;

    /* Detalles físicos */
    private String tipoInmueble;
    private String areaConstruida;
    private String areaTerreno;
    private String areaPrivada;
    private String habitaciones;
    private String banos;
    private String estacionamiento;
    private String piso;
    private String estadoFisico;
    private String anioConstruccion;

    /* Encargado */
    private String encargado;

    /* Características y galería */
    private List<String> caracteristicasInternas;
    private List<String> caracteristicasExternas;
    private List<String> galeriasImagenes;
}
