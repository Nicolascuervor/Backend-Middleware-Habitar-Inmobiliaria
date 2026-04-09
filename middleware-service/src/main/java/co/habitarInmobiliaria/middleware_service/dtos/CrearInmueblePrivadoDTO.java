package co.habitarinmobiliaria.middleware_service.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CrearInmueblePrivadoDTO {

    /* Único dato obligatorio del formulario (las imágenes se validan en multipart) */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    private String titulo;

    private String tipoNegocio;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Long precio;

    private Long valorAdministracion;

    private String ubicacion;

    private String zona;
    private String direccion;

    @Min(value = 0, message = "El estrato debe ser 0 o superior")
    private Integer estrato;

    private String tipoInmueble;

    @Min(value = 0, message = "El área construida no puede ser negativa")
    private Double areaConstruida;

    private Double areaTerreno;
    private Double areaPrivada;

    @Min(value = 0, message = "No puede haber habitaciones negativas")
    private Integer habitaciones;

    @Min(value = 0, message = "No puede haber baños negativos")
    private Integer banos;

    private Integer estacionamiento;
    private Integer piso;
    private String estadoFisico;
    private Integer anioConstruccion;

    private String descripcion;

    private List<String> caracteristicasInternas;
    private List<String> caracteristicasExternas;

    private String idDueno;

    private String idContacto;

    /** Reservado; las imágenes reales llegan en multipart {@code imagenes}. */
    private List<String> imagenesUrls;
}
