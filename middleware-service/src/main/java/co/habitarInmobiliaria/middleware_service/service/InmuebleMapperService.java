package co.habitarinmobiliaria.middleware_service.service;

import static co.habitarinmobiliaria.middleware_service.constans.InmuebleConstants.*;

import co.habitarinmobiliaria.middleware_service.dtos.InmuebleDetalleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.wasi.WasiInmuebleDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InmuebleMapperService {

    public VitrinaInmuebleDTO mapAirtableToVitrina(JsonNode airtableRecord,
            String estadoInmueble, String urlOriginal) {
        if (airtableRecord == null || !airtableRecord.has(FIELD_FIELDS)) {
            return null;
        }

        JsonNode fields = airtableRecord.get(FIELD_FIELDS);
        String idAirtable = airtableRecord.get("id").asText();

        /* Formatear precio */
        long precioRaw = fields.has(FIELD_PRECIO) ? fields.get(FIELD_PRECIO).asLong() : 0L;
        String precioFormateado = "$ " + String.format("%,d", precioRaw).replace(',', '.');

        /* Extraer imagen principal */
        String imagenPrincipal = extraerImagenPrincipalAirtable(fields);

        return VitrinaInmuebleDTO.builder()
                .id(idAirtable)
                .titulo(obtenerCampoAirtable(fields, FIELD_TITULO, "Propiedad Exclusiva"))
                .precioFormateado(precioFormateado)
                .ubicacion(obtenerCampoAirtable(fields, FIELD_UBICACION, FIELD_A_SOLICITUD))
                .habitaciones(obtenerCampoAirtable(fields, FIELD_HABITACIONES, "0"))
                .banos(obtenerCampoAirtable(fields, FIELD_BANOS, "0"))
                .estado(estadoInmueble)
                .area(fields.has(FIELD_AREA_CONSTRUIDA) ? fields.get(FIELD_AREA_CONSTRUIDA).asText() + " m²" : "N/A")
                .imagenUrl(imagenPrincipal)
                .imagenPrincipal(imagenPrincipal)
                .estadoActualCliente(estadoInmueble)
                .url(urlOriginal)
                .build();
    }

    @SuppressWarnings("unchecked")
    public VitrinaInmuebleDTO mapHubSpotToVitrina(Map<String, Object> map, String estadoInmueble, String urlOriginal) {
        if (map == null) return null;

        Object precioRaw = map.get("precio");
        String precioFormateado = "Consultar";
        if (precioRaw != null) {
            try {
                long precio = Long.parseLong(precioRaw.toString());
                precioFormateado = "$ " + String.format("%,d", precio).replace(',', '.');
            } catch (Exception ignored) {}
        }

        List<String> imagenes = (List<String>) map.get("imagenes");
        String imagenPrincipal = (imagenes != null && !imagenes.isEmpty()) ? imagenes.get(0) : "https://via.placeholder.com/400x300?text=Sin+Imagen";

        return VitrinaInmuebleDTO.builder()
                .id((String) map.get("codigoIdentificador"))
                .titulo((String) map.get("titulo"))
                .precioFormateado(precioFormateado)
                .ubicacion((String) map.get("ubicacion"))
                .habitaciones(map.get("habitaciones") != null ? map.get("habitaciones").toString() : "0")
                .banos(map.get("banos") != null ? map.get("banos").toString() : "0")
                .estado(estadoInmueble)
                .area(map.get("areaConstruida") != null ? map.get("areaConstruida").toString() + " m²" : "N/A")
                .imagenUrl(imagenPrincipal)
                .imagenPrincipal(imagenPrincipal)
                .estadoActualCliente(estadoInmueble)
                .url(urlOriginal)
                .build();
    }


    /* Mapear Wasi a vitrina con estado */
    public VitrinaInmuebleDTO mapToVitrina(WasiInmuebleDTO source, String estadoInmueble, String urlOriginal) {
        String precioMostrar = "true".equals(source.getForRent())
                ? source.getRentPriceLabel()
                : source.getSalePriceLabel();

        String descLimpia = limpiarDescripcion(source.getObservations());

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
            if (idPotencial.matches("\\d+") || idPotencial.startsWith("rec") || idPotencial.matches("[a-zA-Z0-9]{8}")) {
                return idPotencial;
            } else {
                log.warn("Formato de ID desconocido en la URL: {}", url);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
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

        /* Resolver precios y tipo de negocio */
        String[] negocioYPrecio = resolverPrecioYTipoNegocio(source);
        String tipoNegocioCalculado = negocioYPrecio[0];
        String precioMostrar = negocioYPrecio[1];

        /* Ubicación completa */
        String ubicacionCompleta = construirUbicacionCompleta(source);

        /* Extraer características */
        List<String> internas = extraerCaracteristicas(source.getFeatures(), FIELD_INTERNAL);
        List<String> externas = extraerCaracteristicas(source.getFeatures(), FIELD_EXTERNAL);

        /* Extraer galería de imágenes */
        List<String> galeria = construirGaleria(source);

        /* Construir DTO final */
        return InmuebleDetalleDTO.builder()
                .titulo(source.getTitle())
                .tipoNegocio(tipoNegocioCalculado)
                .precioFormateado(precioMostrar)
                .valorAdministracion(obtenerStringODefault(source.getMaintenanceFee(), "N/A", "$"))
                .estadoActualCliente(estadoInmueble)
                .ubicacion(ubicacionCompleta)
                .zona(obtenerStringODefault(source.getZoneLabel(), "No especificada"))
                .direccion(obtenerStringODefault(source.getAddress(), FIELD_A_SOLICITUD))
                .estrato(obtenerStringODefault(source.getStratum(), "N/A"))
                .tipoInmueble(obtenerStringODefault(source.getPropertyTypeLabel(), "Inmueble"))
                .areaConstruida(obtenerStringODefault(source.getBuiltArea(), "N/A", "", " m²"))
                .areaTerreno(obtenerStringODefault(source.getArea(), "N/A", "", " m²"))
                .areaPrivada(obtenerStringODefault(source.getPrivateArea(), "N/A", "", " m²"))
                .habitaciones(obtenerStringODefault(source.getBedrooms(), "0"))
                .banos(obtenerStringODefault(source.getBathrooms(), "0"))
                .estacionamiento(obtenerStringODefault(source.getGarages(), "0"))
                .piso(obtenerStringODefault(source.getFloor(), "N/A"))
                .estadoFisico(obtenerStringODefault(source.getPropertyConditionLabel(), "N/A"))
                .anioConstruccion(obtenerStringODefault(source.getBuildingDate(), "N/A"))
                .caracteristicasInternas(internas)
                .caracteristicasExternas(externas)
                .galeriasImagenes(galeria)
                .build();
    }

    public InmuebleDetalleDTO mapAirtableToDetalle(JsonNode airtableRecord,
            String estadoInmueble) {
        if (airtableRecord == null || !airtableRecord.has(FIELD_FIELDS)) {
            throw new IllegalArgumentException("El registro de Airtable está vacío o no tiene el formato esperado.");
        }

        JsonNode fields = airtableRecord.get(FIELD_FIELDS);

        /* Extraer y formatear precio */
        long precioRaw = fields.has(FIELD_PRECIO) ? fields.get(FIELD_PRECIO).asLong() : 0L;
        String precioFormateado = "$ " + String.format("%,d", precioRaw).replace(',', '.');

        /* Extraer administración */
        String valorAdministracion = extraerAdministracion(fields);

        /* Características */
        List<String> internas = extraerListaTextos(fields, FIELD_CARACT_INTERNAS);
        List<String> externas = extraerListaTextos(fields, FIELD_CARACT_EXTERNAS);

        /* Galería de imágenes */
        List<String> galeria = extraerGaleriaAirtable(fields);

        return construirDetalleAirtable(fields, estadoInmueble, precioFormateado,
                valorAdministracion, internas, externas, galeria);
    }

    @SuppressWarnings("unchecked")
    public InmuebleDetalleDTO mapHubSpotToDetalle(Map<String, Object> map, String estadoInmueble) {
        if (map == null) return null;

        Object precioRaw = map.get("precio");
        String precioFormateado = "Consultar";
        if (precioRaw != null) {
            try {
                long precio = Long.parseLong(precioRaw.toString());
                precioFormateado = "$ " + String.format("%,d", precio).replace(',', '.');
            } catch (Exception ignored) {}
        }

        Object adminRaw = map.get("valorAdministracion");
        String valorAdministracion = "N/A";
        if (adminRaw != null) {
            try {
                long admin = Long.parseLong(adminRaw.toString());
                valorAdministracion = "$ " + String.format("%,d", admin).replace(',', '.');
            } catch (Exception ignored) {}
        }

        List<String> internas = map.get("caracteristicasInternas") != null ? (List<String>) map.get("caracteristicasInternas") : new ArrayList<>();
        List<String> externas = map.get("caracteristicasExternas") != null ? (List<String>) map.get("caracteristicasExternas") : new ArrayList<>();
        List<String> galeria = map.get("imagenes") != null ? (List<String>) map.get("imagenes") : new ArrayList<>();

        return InmuebleDetalleDTO.builder()
                .titulo((String) map.get("titulo"))
                .tipoNegocio((String) map.get("tipoNegocio"))
                .precioFormateado(precioFormateado)
                .valorAdministracion(valorAdministracion)
                .estadoActualCliente(estadoInmueble)
                .ubicacion((String) map.get("ubicacion"))
                .zona(map.get("zona") != null ? map.get("zona").toString() : "N/A")
                .direccion(map.get("direccion") != null ? map.get("direccion").toString() : "A solicitud")
                .estrato(map.get("estrato") != null ? map.get("estrato").toString() : "N/A")
                .tipoInmueble(map.get("tipoInmueble") != null ? map.get("tipoInmueble").toString() : "Inmueble")
                .areaConstruida(map.get("areaConstruida") != null ? map.get("areaConstruida").toString() + " m²" : "N/A")
                .areaTerreno(map.get("areaTerreno") != null ? map.get("areaTerreno").toString() + " m²" : "N/A")
                .areaPrivada(map.get("areaPrivada") != null ? map.get("areaPrivada").toString() + " m²" : "N/A")
                .habitaciones(map.get("habitaciones") != null ? map.get("habitaciones").toString() : "0")
                .banos(map.get("banos") != null ? map.get("banos").toString() : "0")
                .estacionamiento(map.get("estacionamiento") != null ? map.get("estacionamiento").toString() : "0")
                .piso(map.get("piso") != null ? map.get("piso").toString() : "N/A")
                .estadoFisico(map.get("estadoFisico") != null ? map.get("estadoFisico").toString() : "N/A")
                .anioConstruccion(map.get("anioConstruccion") != null ? map.get("anioConstruccion").toString() : "N/A")
                .caracteristicasInternas(internas)
                .caracteristicasExternas(externas)
                .galeriasImagenes(galeria)
                .build();
    }

    private String limpiarDescripcion(String observations) {
        if (observations == null || observations.isEmpty()) {
            return "Sin descripción disponible.";
        }
        String textoDecodificado = StringEscapeUtils.unescapeHtml4(observations);
        String sinTags = textoDecodificado.replaceAll("<[^>]*+>", " ");
        String limpio = sinTags.replaceAll("\\s+", " ").trim();
        return limpio.length() > 120 ? limpio.substring(0, 120) + "..." : limpio;
    }

    private String extraerImagenPrincipalAirtable(JsonNode fields) {
        if (fields.has(FIELD_IMAGENES) && fields.get(FIELD_IMAGENES).isArray()
                && fields.get(FIELD_IMAGENES).size() > 0) {
            JsonNode primeraImg = fields.get(FIELD_IMAGENES).get(0);
            if (primeraImg.has("url")) {
                return primeraImg.get("url").asText();
            }
        }
        return "https://via.placeholder.com/600x400?text=Inmueble+Privado";
    }

    private String[] resolverPrecioYTipoNegocio(WasiInmuebleDTO source) {
        boolean esVenta = "true".equalsIgnoreCase(source.getForSale());
        boolean esAlquiler = "true".equalsIgnoreCase(source.getForRent());

        if (esVenta && esAlquiler) {
            String pVenta = obtenerStringODefault(source.getSalePriceLabel(), "N/A");
            String pAlquiler = obtenerStringODefault(source.getRentPriceLabel(), "N/A");
            return new String[] { "Venta y Alquiler", "Venta: " + pVenta + " | Alquiler: " + pAlquiler };
        }
        if (esVenta) {
            return new String[] { "Venta", obtenerStringODefault(source.getSalePriceLabel(), "Consultar") };
        }
        if (esAlquiler) {
            return new String[] { "Alquiler", obtenerStringODefault(source.getRentPriceLabel(), "Consultar") };
        }
        return new String[] { "No especificado", "Consultar precio" };
    }

    private String construirUbicacionCompleta(WasiInmuebleDTO source) {
        return String.format("%s, %s, %s",
                obtenerStringODefault(source.getCountryLabel(), ""),
                obtenerStringODefault(source.getRegionLabel(), ""),
                obtenerStringODefault(source.getCityLabel(), ""))
                .replaceAll("^, |, $|(?<=, )(?=,)", "")
                .trim();
    }

    private List<String> extraerCaracteristicas(JsonNode features, String key) {
        List<String> resultado = new ArrayList<>();
        if (features == null || !features.isObject() || !features.has(key) || !features.get(key).isArray()) {
            return resultado;
        }
        for (JsonNode nodo : features.get(key)) {
            if (nodo.has(FIELD_NOMBRE)) {
                resultado.add(nodo.get(FIELD_NOMBRE).asText());
            }
        }
        return resultado;
    }

    private List<String> construirGaleria(WasiInmuebleDTO source) {
        List<String> galeria = new ArrayList<>();

        /* Imagen principal primero */
        if (source.getMainImage() != null && source.getMainImage().getUrl() != null) {
            galeria.add(source.getMainImage().getUrl());
        }

        /* Resto de imágenes */
        if (source.getGalleries() != null && source.getGalleries().isArray()) {
            for (JsonNode galeriaNodo : source.getGalleries()) {
                agregarImagenesDeGaleria(galeriaNodo, galeria);
            }
        }
        return galeria;
    }

    private void agregarImagenesDeGaleria(JsonNode galeriaNodo, List<String> galeria) {
        if (!galeriaNodo.isObject()) {
            return;
        }
        galeriaNodo.properties().forEach(entry -> {
            JsonNode valorImagen = entry.getValue();
            if (valorImagen.isObject() && valorImagen.has("url")) {
                String urlImagen = valorImagen.get("url").asText();
                if (urlImagen != null && !galeria.contains(urlImagen)) {
                    galeria.add(urlImagen);
                }
            }
        });
    }

    private String extraerAdministracion(JsonNode fields) {
        if (fields.has("Valor Administración")) {
            long adminRaw = fields.get("Valor Administración").asLong();
            return "$ " + String.format("%,d", adminRaw).replace(',', '.');
        }
        return "N/A";
    }

    private List<String> extraerListaTextos(JsonNode fields, String key) {
        List<String> resultado = new ArrayList<>();
        if (fields.has(key) && fields.get(key).isArray()) {
            fields.get(key).forEach(nodo -> resultado.add(nodo.asText()));
        }
        return resultado;
    }

    private List<String> extraerGaleriaAirtable(JsonNode fields) {
        List<String> galeria = new ArrayList<>();
        if (fields.has(FIELD_IMAGENES) && fields.get(FIELD_IMAGENES).isArray()) {
            fields.get(FIELD_IMAGENES).forEach(imgNodo -> {
                if (imgNodo.has("url")) {
                    galeria.add(imgNodo.get("url").asText());
                }
            });
        }
        return galeria;
    }

    private InmuebleDetalleDTO construirDetalleAirtable(JsonNode fields, String estadoInmueble,
            String precioFormateado, String valorAdministracion,
            List<String> internas, List<String> externas, List<String> galeria) {
        return InmuebleDetalleDTO.builder()
                .titulo(obtenerCampoAirtable(fields, FIELD_TITULO, "Sin título"))
                .tipoNegocio(obtenerCampoAirtable(fields, "Tipo Negocio", "N/A"))
                .precioFormateado(precioFormateado)
                .valorAdministracion(valorAdministracion)
                .estadoActualCliente(estadoInmueble)
                .ubicacion(obtenerCampoAirtable(fields, FIELD_UBICACION, "N/A"))
                .zona(obtenerCampoAirtable(fields, "Zona", "N/A"))
                .direccion(obtenerCampoAirtable(fields, "Dirección", FIELD_A_SOLICITUD))
                .estrato(obtenerCampoAirtable(fields, "Estrato", "N/A"))
                .tipoInmueble(obtenerCampoAirtable(fields, "Tipo Inmueble", "Inmueble"))
                .areaConstruida(
                        fields.has(FIELD_AREA_CONSTRUIDA) ? fields.get(FIELD_AREA_CONSTRUIDA).asText() + " m²" : "N/A")
                .areaTerreno(
                        fields.has("Área Terreno")
                                ? fields.get("Área Terreno").asText() + " m²"
                                : "N/A")
                .areaPrivada(
                        fields.has("Área Privada")
                                ? fields.get("Área Privada").asText() + " m²"
                                : "N/A")
                .habitaciones(obtenerCampoAirtable(fields, FIELD_HABITACIONES, "0"))
                .banos(obtenerCampoAirtable(fields, FIELD_BANOS, "0"))
                .estacionamiento(obtenerCampoAirtable(fields, "Estacionamiento", "0"))
                .piso(obtenerCampoAirtable(fields, "Piso", "N/A"))
                .estadoFisico(obtenerCampoAirtable(fields, "Estado Físico", "N/A"))
                .anioConstruccion(obtenerCampoAirtable(fields, "Año Construcción", "N/A"))
                .caracteristicasInternas(internas)
                .caracteristicasExternas(externas)
                .galeriasImagenes(galeria)
                .build();
    }

    private String obtenerCampoAirtable(JsonNode fields, String key, String defaultVal) {
        return fields.has(key) ? fields.get(key).asText() : defaultVal;
    }

    private String obtenerStringODefault(String value, String defaultVal) {
        return value != null ? value : defaultVal;
    }

    private String obtenerStringODefault(String value, String defaultVal, String prefix) {
        return value != null ? prefix + value : defaultVal;
    }

    private String obtenerStringODefault(String value, String defaultVal, String prefix, String suffix) {
        return value != null ? prefix + value + suffix : defaultVal;
    }
}