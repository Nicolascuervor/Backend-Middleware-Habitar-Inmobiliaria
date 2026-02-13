package co.habitarinmobiliaria.middleware_service.service;


import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.WasiInmuebleDTO;
import org.springframework.stereotype.Service;
import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Pattern;

@Service
public class InmuebleMapperService {

    // Regex para capturar números al final de una URL
    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)$");

    /**
     * Convierte el objeto complejo de Wasi en el objeto simple para el Abuelo.
     */
    public VitrinaInmuebleDTO mapToVitrina(WasiInmuebleDTO source) {
        // 1. Determinar precio a mostrar (Prioridad Renta vs Venta)
        // Si for_rent es "true", mostramos el precio de arriendo, sino el de venta.
        String precioMostrar = "true".equals(source.getForRent())
                ? source.getRentPriceLabel()
                : source.getSalePriceLabel();

        // 2. Lógica de Limpieza de Descripción (Sanitización)
        String descLimpia = "";
        if (source.getObservations() != null && !source.getObservations().isEmpty()) {
            // A. Decodificar entidades HTML (ej: "&ntilde;" -> "ñ", "&oacute;" -> "ó")
            String textoDecodificado = StringEscapeUtils.unescapeHtml4(source.getObservations());

            // B. Eliminar etiquetas HTML (ej: <p>, <br>, <strong>) usando Regex
            // Reemplazamos por un espacio para que no se peguen las palabras
            String sinTags = textoDecodificado.replaceAll("\\<.*?\\>", " ");

            // C. Normalizar espacios (quitar dobles espacios, tabulaciones y saltos de línea extra)
            descLimpia = sinTags.replaceAll("\\s+", " ").trim();

            // D. Truncar a 120 caracteres para la vista previa (evitar textos infinitos)
            if (descLimpia.length() > 120) {
                descLimpia = descLimpia.substring(0, 120) + "...";
            }
        } else {
            descLimpia = "Sin descripción disponible.";
        }

        // 3. Construcción del Objeto Final (Builder)
        return VitrinaInmuebleDTO.builder()
                .id(source.getIdProperty())
                .titulo(source.getTitle())
                .precioFormateado(precioMostrar)
                // Combinamos Ciudad y Zona para dar contexto geográfico
                .ubicacion(source.getCityLabel() + " - " + source.getZoneLabel())
                // Validación de nulos en la imagen para evitar romper el frontend
                .imagenUrl(source.getMainImage() != null ? source.getMainImage().getUrl() : "https://via.placeholder.com/400x300?text=Sin+Imagen")
                .descripcionCorta(descLimpia)
                .esDestacado(false) // Por defecto false, lógica expandible a futuro
                .build();
    }


    public String extraerIdDeUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        // Opción A: Split por '/' (Más rápida pero asume estructura fija)
        String[] partes = url.split("/");
        String posibleId = partes[partes.length - 1];
        // Validación extra: asegurar que sea numérico
        if (posibleId.matches("\\d+")) {
            return posibleId;
        }
        return null; // O lanzar excepción personalizada
    }
}
