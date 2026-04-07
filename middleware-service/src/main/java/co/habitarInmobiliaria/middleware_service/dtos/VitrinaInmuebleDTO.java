package co.habitarinmobiliaria.middleware_service.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/* DTO optimizado para la tarjeta de inmueble en el frontend */
@Data
@Builder
@Schema(description = "Objeto optimizado para la tarjeta de inmueble en el Frontend")
public class VitrinaInmuebleDTO {

    @Schema(description = "Identificador único del inmueble en Wasi", example = "9773703")
    private String id;

    @Schema(description = "Título comercial del inmueble", example = "Apartamento en Puerto Espejo")
    private String titulo;

    @Schema(description = "Precio ya formateado con moneda", example = "$1.050.000")
    private String precioFormateado;

    @Schema(description = "Ubicación compuesta (Ciudad - Zona)", example = "Armenia - Puerto Espejo")
    private String ubicacion;

    @Schema(description = "URL pública de la imagen principal", example = "https://image.wasi.co/...")
    private String imagenUrl;

    @Schema(description = "Descripción limpia de HTML y truncada a 120 caracteres", example = "Hermoso apartamento con vista...")
    private String descripcionCorta;

    @Schema(description = "Indica si debe resaltarse en la UI (botones grandes)", example = "false")
    private boolean esDestacado;

    @Schema(description = "Estado de revisión del inmueble por parte del cliente", example = "APROBADO")
    private String estado;

    private String urlReferencia;

    private String habitaciones;

    private String banos;

    private String area;

    private String imagenPrincipal;

    private String estadoActualCliente;

    private String url;
}