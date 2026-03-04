package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.dtos.InmuebleDetalleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.wasi.WasiInmuebleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.text.StringEscapeUtils;
import java.util.regex.Pattern;

@Service
@Slf4j
public class InmuebleMapperService {

    public VitrinaInmuebleDTO mapAirtableToVitrina(com.fasterxml.jackson.databind.JsonNode airtableRecord,
            String estadoInmueble, String urlOriginal) {
        if (airtableRecord == null || !airtableRecord.has("fields")) {
            return null;
        }

        com.fasterxml.jackson.databind.JsonNode fields = airtableRecord.get("fields");
        String idAirtable = airtableRecord.get("id").asText();

        /* Formatear precio */
        long precioRaw = fields.has("Precio") ? fields.get("Precio").asLong() : 0L;
        String precioFormateado = "$ " + String.format("%,d", precioRaw).replace(',', '.');

        /* Extraer imagen principal */
        String imagenPrincipal = "https://via.placeholder.com/600x400?text=Inmueble+Privado";
        if (fields.has("Imágenes") && fields.get("Imágenes").isArray() && fields.get("Imágenes").size() > 0) {
            com.fasterxml.jackson.databind.JsonNode primeraImg = fields.get("Imágenes").get(0);
            if (primeraImg.has("url")) {
                imagenPrincipal = primeraImg.get("url").asText();
            }
        }

        return VitrinaInmuebleDTO.builder()
                .id(idAirtable)
                .titulo(fields.has("Título") ? fields.get("Título").asText() : "Propiedad Exclusiva")
                .precioFormateado(precioFormateado)
                .ubicacion(fields.has("Ubicación") ? fields.get("Ubicación").asText() : "A solicitud")
                .habitaciones(fields.has("Habitaciones") ? fields.get("Habitaciones").asText() : "0")
                .banos(fields.has("Baños") ? fields.get("Baños").asText() : "0")
                .estado(estadoInmueble)
                .area(fields.has("Área Construida") ? fields.get("Área Construida").asText() + " m²" : "N/A")
                .imagenUrl(imagenPrincipal)
                .imagenPrincipal(imagenPrincipal)
                .estadoActualCliente(estadoInmueble)
                .url(urlOriginal)
                .build();
    }

    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)$");

    /* Mapear Wasi a vitrina con estado */
    public VitrinaInmuebleDTO mapToVitrina(WasiInmuebleDTO source, String estadoInmueble, String urlOriginal) {
        String precioMostrar = "true".equals(source.getForRent())
                ? source.getRentPriceLabel()
                : source.getSalePriceLabel();

        String descLimpia = "Sin descripción disponible.";
        if (source.getObservations() != null && !source.getObservations().isEmpty()) {
            String textoDecodificado = StringEscapeUtils.unescapeHtml4(source.getObservations());
            String sinTags = textoDecodificado.replaceAll("\\<.*?\\>", " ");
            descLimpia = sinTags.replaceAll("\\s+", " ").trim();
            if (descLimpia.length() > 120) {
                descLimpia = descLimpia.substring(0, 120) + "...";
            }
        }

        return VitrinaInmuebleDTO.builder()
                .id(source.getIdProperty())
                .titulo(source.getTitle())
                .precioFormateado(precioMostrar)
                .ubicacion(source.getCityLabel() + " - " + source.getZoneLabel())
                .imagenUrl(source.getMainImage() != null ? source.getMainImage().getUrl()
                        : "https://via.placeholder.com/400x300?text=Sin+Imagen")
                .descripcionCorta(descLimpia)
                .esDestacado(false)
                .estado(estadoInmueble)
                .urlReferencia(urlOriginal)
                .build();
    }

    /* Extraer ID limpio de una URL */
    public String extraerIdDeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            /* Limpiar anclas */
            String urlLimpia = url.contains("#") ? url.split("#")[0] : url;

            /* Extraer último segmento */
            String[] partes = urlLimpia.split("/");
            String idPotencial = partes[partes.length - 1];

            /* Quitar sufijo de estado si existe */
            if (idPotencial.contains("-")) {
                idPotencial = idPotencial.split("-")[0];
            }

            /* Validar formato del ID */
            if (idPotencial.matches("\\d+") || idPotencial.startsWith("rec")) {
                return idPotencial;
            } else {
                log.warn("Formato de ID desconocido en la URL: {}", url);
            }

        } catch (Exception e) {
            log.error("Error al extraer el ID de la URL: {}", url, e);
        }

        return null;
    }

    public String extraerEstadoDeUrl(String url) {
        if (url == null || url.isEmpty())
            return "SIN_REVISAR";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-([A-Z]+)$");
        java.util.regex.Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            /* Retorna el estado encontrado */
            return matcher.group(1);
        }
        return "SIN_REVISAR";
    }

    public InmuebleDetalleDTO mapToDetalle(WasiInmuebleDTO source, String estadoInmueble) {

        /* 🚀 Lógica de precios y tipo de negocio (Soporte Dual y Defensivo) */
        boolean esVenta = "true".equalsIgnoreCase(source.getForSale());
        boolean esAlquiler = "true".equalsIgnoreCase(source.getForRent());

        String tipoNegocioCalculado = "No especificado";
        String precioMostrar = "Consultar precio";

        if (esVenta && esAlquiler) {
            tipoNegocioCalculado = "Venta y Alquiler";
            String pVenta = source.getSalePriceLabel() != null ? source.getSalePriceLabel() : "N/A";
            String pAlquiler = source.getRentPriceLabel() != null ? source.getRentPriceLabel() : "N/A";
            // Concatenamos ambos precios para que el frontend los reciba listos para mostrar
            precioMostrar = "Venta: " + pVenta + " | Alquiler: " + pAlquiler;

        } else if (esVenta) {
            tipoNegocioCalculado = "Venta";
            precioMostrar = source.getSalePriceLabel() != null ? source.getSalePriceLabel() : "Consultar";

        } else if (esAlquiler) {
            tipoNegocioCalculado = "Alquiler";
            precioMostrar = source.getRentPriceLabel() != null ? source.getRentPriceLabel() : "Consultar";
        }

        /* Lógica de ubicación */
        String ubicacionCompleta = String.format("%s, %s, %s",
                        source.getCountryLabel() != null ? source.getCountryLabel() : "",
                        source.getRegionLabel() != null ? source.getRegionLabel() : "",
                        source.getCityLabel() != null ? source.getCityLabel() : "").replaceAll("^, |, $|(?<=, )(?=,)", "")
                .trim();

        /* Extraer características */
        java.util.List<String> internas = new java.util.ArrayList<>();
        java.util.List<String> externas = new java.util.ArrayList<>();

        if (source.getFeatures() != null && source.getFeatures().isObject()) {

            /* Características internas */
            if (source.getFeatures().has("internal") && source.getFeatures().get("internal").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode nodo : source.getFeatures().get("internal")) {
                    if (nodo.has("nombre")) {
                        internas.add(nodo.get("nombre").asText());
                    }
                }
            }

            /* Características externas */
            if (source.getFeatures().has("external") && source.getFeatures().get("external").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode nodo : source.getFeatures().get("external")) {
                    if (nodo.has("nombre")) {
                        externas.add(nodo.get("nombre").asText());
                    }
                }
            }
        }

        /* Extraer galería de imágenes */
        java.util.List<String> galeria = new java.util.ArrayList<>();

        /* Imagen principal primero */
        if (source.getMainImage() != null && source.getMainImage().getUrl() != null) {
            galeria.add(source.getMainImage().getUrl());
        }

        /* Resto de imágenes */
        if (source.getGalleries() != null && source.getGalleries().isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode galeriaNodo : source.getGalleries()) {

                if (galeriaNodo.isObject()) {
                    galeriaNodo.fields().forEachRemaining(entry -> {
                        com.fasterxml.jackson.databind.JsonNode valorImagen = entry.getValue();

                        /* Solo agregar si tiene URL y no está repetida */
                        if (valorImagen.isObject() && valorImagen.has("url")) {
                            String urlImagen = valorImagen.get("url").asText();

                            if (urlImagen != null && !galeria.contains(urlImagen)) {
                                galeria.add(urlImagen);
                            }
                        }
                    });
                }
            }
        }

        /* Construir DTO final */
        return InmuebleDetalleDTO.builder()
                .titulo(source.getTitle())
                // Inyectamos las variables locales que procesamos arriba
                .tipoNegocio(tipoNegocioCalculado)
                .precioFormateado(precioMostrar)

                .valorAdministracion(source.getMaintenanceFee() != null ? "$" + source.getMaintenanceFee() : "N/A")
                .estadoActualCliente(estadoInmueble)

                .ubicacion(ubicacionCompleta)
                .zona(source.getZoneLabel() != null ? source.getZoneLabel() : "No especificada")
                .direccion(source.getAddress() != null ? source.getAddress() : "A solicitud")
                .estrato(source.getStratum() != null ? source.getStratum() : "N/A")

                .tipoInmueble(source.getPropertyTypeLabel() != null ? source.getPropertyTypeLabel() : "Inmueble")
                .areaConstruida(source.getBuiltArea() != null ? source.getBuiltArea() + " m²" : "N/A")
                .areaTerreno(source.getArea() != null ? source.getArea() + " m²" : "N/A")
                .areaPrivada(source.getPrivateArea() != null ? source.getPrivateArea() + " m²" : "N/A")
                .habitaciones(source.getBedrooms() != null ? source.getBedrooms() : "0")
                .banos(source.getBathrooms() != null ? source.getBathrooms() : "0")
                .estacionamiento(source.getGarages() != null ? source.getGarages() : "0")
                .piso(source.getFloor() != null ? source.getFloor() : "N/A")
                .estadoFisico(source.getPropertyConditionLabel() != null ? source.getPropertyConditionLabel() : "N/A")
                .anioConstruccion(source.getBuildingDate() != null ? source.getBuildingDate() : "N/A")

                .caracteristicasInternas(internas)
                .caracteristicasExternas(externas)
                .galeriasImagenes(galeria)
                .build();
    }

    public InmuebleDetalleDTO mapAirtableToDetalle(com.fasterxml.jackson.databind.JsonNode airtableRecord,
            String estadoInmueble) {
        if (airtableRecord == null || !airtableRecord.has("fields")) {
            throw new RuntimeException("El registro de Airtable está vacío o no tiene el formato esperado.");
        }

        com.fasterxml.jackson.databind.JsonNode fields = airtableRecord.get("fields");

        /* Extraer y formatear precio */
        long precioRaw = fields.has("Precio") ? fields.get("Precio").asLong() : 0L;
        String precioFormateado = "$ " + String.format("%,d", precioRaw).replace(',', '.');

        /* Extraer administración */
        String valorAdministracion = "N/A";
        if (fields.has("Valor Administración")) {
            long adminRaw = fields.get("Valor Administración").asLong();
            valorAdministracion = "$ " + String.format("%,d", adminRaw).replace(',', '.');
        }

        /* Características internas */
        java.util.List<String> internas = new java.util.ArrayList<>();
        if (fields.has("Características Internas") && fields.get("Características Internas").isArray()) {
            fields.get("Características Internas").forEach(nodo -> internas.add(nodo.asText()));
        }

        /* Características externas */
        java.util.List<String> externas = new java.util.ArrayList<>();
        if (fields.has("Características Externas") && fields.get("Características Externas").isArray()) {
            fields.get("Características Externas").forEach(nodo -> externas.add(nodo.asText()));
        }

        /* Galería de imágenes */
        java.util.List<String> galeria = new java.util.ArrayList<>();
        if (fields.has("Imágenes") && fields.get("Imágenes").isArray()) {
            fields.get("Imágenes").forEach(imgNodo -> {
                if (imgNodo.has("url")) {
                    galeria.add(imgNodo.get("url").asText());
                }
            });
        }

        return InmuebleDetalleDTO.builder()
                .titulo(fields.has("Título") ? fields.get("Título").asText() : "Sin título")
                .tipoNegocio(fields.has("Tipo Negocio") ? fields.get("Tipo Negocio").asText() : "N/A")
                .precioFormateado(precioFormateado)
                .valorAdministracion(valorAdministracion)
                .estadoActualCliente(estadoInmueble)

                .ubicacion(fields.has("Ubicación") ? fields.get("Ubicación").asText() : "N/A")
                .zona(fields.has("Zona") ? fields.get("Zona").asText() : "N/A")
                .direccion(fields.has("Dirección") ? fields.get("Dirección").asText() : "A solicitud")
                .estrato(fields.has("Estrato") ? fields.get("Estrato").asText() : "N/A")

                .tipoInmueble(fields.has("Tipo Inmueble") ? fields.get("Tipo Inmueble").asText() : "Inmueble")
                .areaConstruida(fields.has("Área Construida") ? fields.get("Área Construida").asText() + " m²" : "N/A")
                .areaTerreno(fields.has("Área Terreno") ? fields.get("Área Terreno").asText() + " m²" : "N/A")
                .areaPrivada(fields.has("Área Privada") ? fields.get("Área Privada").asText() + " m²" : "N/A")
                .habitaciones(fields.has("Habitaciones") ? fields.get("Habitaciones").asText() : "0")
                .banos(fields.has("Baños") ? fields.get("Baños").asText() : "0")
                .estacionamiento(fields.has("Estacionamiento") ? fields.get("Estacionamiento").asText() : "0")
                .piso(fields.has("Piso") ? fields.get("Piso").asText() : "N/A")
                .estadoFisico(fields.has("Estado Físico") ? fields.get("Estado Físico").asText() : "N/A")
                .anioConstruccion(fields.has("Año Construcción") ? fields.get("Año Construcción").asText() : "N/A")

                /* Dueño asumido por contexto de Airtable */
                //.encargado(fields.has("ID Dueño") ? "Asesor Privado" : "No asignado")

                .caracteristicasInternas(internas)
                .caracteristicasExternas(externas)
                .galeriasImagenes(galeria)
                .build();
    }
}