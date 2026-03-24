package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.dtos.CrearInmueblePrivadoDTO;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InmueblePrivadoService {

    private final HubSpotFilesService hubSpotFilesService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    /* Extensiones y tipos MIME permitidos para imágenes */
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANO_MAXIMO = 10 * 1024 * 1024; /* 10 MB */

    public Map<String, String> crearInmueble(CrearInmueblePrivadoDTO dto, MultipartFile[] imagenes) {
        log.info("Iniciando creación de inmueble privado: {}", dto.getTitulo());

        /* Validar imágenes */
        validarImagenes(imagenes);

        /* Generar nombre único para la carpeta */
        String idCorto = UUID.randomUUID().toString().substring(0, 8);
        String nombreCarpeta = dto.getTitulo().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s-]", "").trim() + "-" + idCorto;

        /* 1. Crear carpeta en HubSpot */
        String folderId = hubSpotFilesService.crearCarpeta(nombreCarpeta);
        log.info("Carpeta creada con ID: {}", folderId);

        /* 2. Subir metadatos como JSON */
        Map<String, Object> metadatos = construirMetadatos(dto);
        hubSpotFilesService.subirMetadatos(folderId, metadatos);
        log.info("Metadatos subidos exitosamente");

        /* 3. Subir cada imagen */
        List<String> urlsImagenes = new ArrayList<>();
        for (int i = 0; i < imagenes.length; i++) {
            MultipartFile imagen = imagenes[i];
            try {
                String extension = obtenerExtension(imagen.getOriginalFilename());
                String nombreImagen = "imagen_" + (i + 1) + extension;
                String contentType = imagen.getContentType() != null ? imagen.getContentType() : "image/jpeg";

                String url = hubSpotFilesService.subirArchivo(
                        folderId, nombreImagen, imagen.getBytes(), contentType);
                urlsImagenes.add(url);
                log.info("Imagen {}/{} subida: {}", (i + 1), imagenes.length, nombreImagen);

            } catch (IOException e) {
                log.error("Error al leer imagen #{}", (i + 1), e);
                throw new ErrorExternoException("Error al procesar la imagen #" + (i + 1));
            }
        }

        log.info("Inmueble creado exitosamente. FolderId: {} | Código: {} | Imágenes: {}", folderId, idCorto, urlsImagenes.size());
        return Map.of("folderId", folderId, "codigoIdentificador", idCorto);
    }

    public Map<String, Object> obtenerInmueblePorCodigo(String codigo) {
        log.info("Buscando inmueble privado por código: {}", codigo);

        /* 1. Buscar ID de la carpeta */
        String folderId = hubSpotFilesService.buscarCarpetaPorNombre(codigo);

        /* 2. Obtener lista de archivos */
        List<Map<String, Object>> archivos = hubSpotFilesService.obtenerArchivosDeCarpeta(folderId);

        String urlMetadatos = null;
        List<String> urlsImagenes = new ArrayList<>();

        for (Map<String, Object> archivo : archivos) {
            String name = (String) archivo.get("name");
            String extension = (String) archivo.get("extension");
            String type = (String) archivo.get("type");
            String url = (String) archivo.get("url");

            if ("metadatos".equals(name) && "json".equals(extension)) {
                urlMetadatos = url;
            } else if ("IMG".equals(type) && url != null) {
                urlsImagenes.add(url);
            }
        }

        if (urlMetadatos == null) {
            throw new ErrorExternoException("No se encontraron los metadatos del inmueble en HubSpot.");
        }

        /* 3. Descargar y parsear metadatos */
        try {
            /* Usamos URI.create para evitar que RestTemplate codifique doblemente los %20 como %2520 */
            String jsonMetadatos = restTemplate.getForObject(java.net.URI.create(urlMetadatos), String.class);
            Map<String, Object> inmueble = objectMapper.readValue(jsonMetadatos, Map.class);
            
            /* 4. Adjuntar imágenes al mapa resultante */
            inmueble.put("imagenes", urlsImagenes);
            inmueble.put("codigoIdentificador", codigo);
            
            log.info("Inmueble {} recuperado exitosamente con {} imágenes", codigo, urlsImagenes.size());
            return inmueble;
            
        } catch (Exception e) {
            log.error("Error al descargar o procesar metadatos.json desde URL: {}", urlMetadatos, e);
            throw new ErrorExternoException("Error al procesar la información del inmueble: " + e.getMessage());
        }
    }

    /* Construir mapa de metadatos desde el DTO */
    private Map<String, Object> construirMetadatos(CrearInmueblePrivadoDTO dto) {
        Map<String, Object> metadatos = new LinkedHashMap<>();

        /* Campos obligatorios */
        metadatos.put("titulo", dto.getTitulo());
        metadatos.put("tipoNegocio", dto.getTipoNegocio());
        metadatos.put("precio", dto.getPrecio());
        metadatos.put("idDueno", dto.getIdDueno());
        metadatos.put("idContacto", dto.getIdContacto());
        metadatos.put("ubicacion", dto.getUbicacion());
        metadatos.put("tipoInmueble", dto.getTipoInmueble());
        metadatos.put("habitaciones", dto.getHabitaciones());
        metadatos.put("banos", dto.getBanos());
        metadatos.put("descripcion", dto.getDescripcion());

        /* Campos opcionales */
        agregarSiNoNull(metadatos, "valorAdministracion", dto.getValorAdministracion());
        agregarSiNoNull(metadatos, "zona", dto.getZona());
        agregarSiNoNull(metadatos, "direccion", dto.getDireccion());
        agregarSiNoNull(metadatos, "estrato", dto.getEstrato());
        agregarSiNoNull(metadatos, "areaConstruida", dto.getAreaConstruida());
        agregarSiNoNull(metadatos, "areaTerreno", dto.getAreaTerreno());
        agregarSiNoNull(metadatos, "areaPrivada", dto.getAreaPrivada());
        agregarSiNoNull(metadatos, "estacionamiento", dto.getEstacionamiento());
        agregarSiNoNull(metadatos, "piso", dto.getPiso());
        agregarSiNoNull(metadatos, "estadoFisico", dto.getEstadoFisico());
        agregarSiNoNull(metadatos, "anioConstruccion", dto.getAnioConstruccion());
        agregarSiNoNull(metadatos, "caracteristicasInternas", dto.getCaracteristicasInternas());
        agregarSiNoNull(metadatos, "caracteristicasExternas", dto.getCaracteristicasExternas());

        return metadatos;
    }

    private void agregarSiNoNull(Map<String, Object> mapa, String key, Object value) {
        if (value != null) {
            mapa.put(key, value);
        }
    }

    /* Validar todas las imágenes */
    private void validarImagenes(MultipartFile[] imagenes) {
        if (imagenes == null || imagenes.length == 0) {
            throw new IllegalArgumentException("Debe incluir al menos una imagen");
        }

        for (int i = 0; i < imagenes.length; i++) {
            MultipartFile img = imagenes[i];

            if (img.isEmpty()) {
                throw new IllegalArgumentException("La imagen #" + (i + 1) + " está vacía");
            }

            if (img.getSize() > TAMANO_MAXIMO) {
                throw new IllegalArgumentException("La imagen #" + (i + 1) + " excede 10 MB");
            }

            String extension = obtenerExtension(img.getOriginalFilename()).toLowerCase();
            if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
                throw new IllegalArgumentException(
                        "La imagen #" + (i + 1) + " tiene extensión no permitida. Solo: " + EXTENSIONES_PERMITIDAS);
            }

            if (img.getContentType() == null || !CONTENT_TYPES_PERMITIDOS.contains(img.getContentType())) {
                throw new IllegalArgumentException("La imagen #" + (i + 1) + " tiene tipo MIME no permitido");
            }
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
        }
        return ".jpg";
    }
}