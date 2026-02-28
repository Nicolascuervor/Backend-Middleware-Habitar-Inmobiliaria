package co.habitarinmobiliaria.middleware_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
public class CrearInmueblePrivadoDTO {

    /* Datos básicos */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 10, max = 100, message = "El título debe tener entre 10 y 100 caracteres")
    private String titulo;

    @NotBlank(message = "El tipo de negocio (Venta/Alquiler) es obligatorio")
    private String tipoNegocio;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Long precio;

    private Long valorAdministracion;

    /* Ubicación */
    @NotBlank(message = "La ubicación es obligatoria (Ej: Colombia, Quindío, Armenia)")
    private String ubicacion;

    private String zona;
    private String direccion;

    @Min(value = 0, message = "El estrato debe ser 0 o superior")
    private Integer estrato;

    /* Detalles físicos */
    @NotBlank(message = "El tipo de inmueble es obligatorio (Ej: Casa, Apartamento)")
    private String tipoInmueble;

    @Min(value = 1, message = "El área construida debe ser mayor a 0")
    private Double areaConstruida;

    private Double areaTerreno;
    private Double areaPrivada;

    @NotNull(message = "El número de habitaciones es obligatorio")
    @Min(value = 0, message = "No puede haber habitaciones negativas")
    private Integer habitaciones;

    @NotNull(message = "El número de baños es obligatorio")
    @Min(value = 0, message = "No puede haber baños negativos")
    private Integer banos;

    private Integer estacionamiento;
    private Integer piso;
    private String estadoFisico;
    private Integer anioConstruccion;

    /* Descripción */
    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    /* Características de selección múltiple */
    private List<String> caracteristicasInternas;
    private List<String> caracteristicasExternas;

    @NotBlank(message = "El ID del dueño/asesor es obligatorio")
    private String idDueno;

    @NotBlank(message = "El ID del contacto/cliente es obligatorio")
    private String idContacto;

    /* URLs de imágenes */
    @NotNull(message = "Debe incluir al menos una imagen")
    @Size(min = 1, message = "Debe subir al menos una imagen del inmueble")
    private List<String> imagenesUrls;
}
