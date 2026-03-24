package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotFileResponseDTO;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotFolderResponseDTO;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class HubSpotFilesService {

    private static final String HUBSPOT_FILES_URL = "https://api.hubapi.com/files/v3/files";
    private static final String HUBSPOT_FOLDERS_URL = "https://api.hubapi.com/files/v3/folders";
    private static final String OPTIONS_JSON = "{\"access\":\"PUBLIC_NOT_INDEXABLE\"}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hubspot.token}")
    private String hubSpotToken;

    @Value("${hubspot.files.parent-folder-id}")
    private String parentFolderId;

    public HubSpotFilesService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /* Crear carpeta en HubSpot Files */
    public String crearCarpeta(String nombreCarpeta) {
        log.info("Creando carpeta en HubSpot: {}", nombreCarpeta);

        HttpHeaders headers = crearHeadersJson();

        Map<String, String> body = Map.of(
                "name", nombreCarpeta,
                "parentFolderId", parentFolderId
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<HubSpotFolderResponseDTO> response = restTemplate.exchange(
                    HUBSPOT_FOLDERS_URL, HttpMethod.POST, request, HubSpotFolderResponseDTO.class);

            if (response.getBody() == null || response.getBody().getId() == null) {
                throw new ErrorExternoException("HubSpot no retornó un ID de carpeta válido");
            }

            String folderId = response.getBody().getId();
            log.info("Carpeta creada exitosamente. FolderId: {}", folderId);
            return folderId;

        } catch (ErrorExternoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al crear carpeta en HubSpot", e);
            throw new ErrorExternoException("Error al crear carpeta en HubSpot: " + e.getMessage());
        }
    }

    /* Subir archivo binario a una carpeta de HubSpot */
    public String subirArchivo(String folderId, String fileName, byte[] contenido, String contentType) {
        log.info("Subiendo archivo '{}' a carpeta {}", fileName, folderId);

        HttpHeaders headers = crearHeadersMultipart();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        /* El archivo como ByteArrayResource con nombre */
        ByteArrayResource fileResource = new ByteArrayResource(contenido) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        /* Headers del part del archivo */
        HttpHeaders filePartHeaders = new HttpHeaders();
        filePartHeaders.setContentType(MediaType.parseMediaType(contentType));
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, filePartHeaders);

        body.add("file", filePart);
        body.add("folderId", folderId);
        body.add("options", OPTIONS_JSON);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<HubSpotFileResponseDTO> response = restTemplate.exchange(
                    HUBSPOT_FILES_URL, HttpMethod.POST, request, HubSpotFileResponseDTO.class);

            if (response.getBody() == null || response.getBody().getUrl() == null) {
                throw new ErrorExternoException("HubSpot no retornó una URL para el archivo");
            }

            String url = response.getBody().getUrl();
            log.info("Archivo subido exitosamente. URL: {}", url);
            return url;

        } catch (ErrorExternoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al subir archivo a HubSpot", e);
            throw new ErrorExternoException("Error al subir archivo a HubSpot: " + e.getMessage());
        }
    }

    /* Subir metadatos como archivo JSON */
    public String subirMetadatos(String folderId, Map<String, Object> metadatos) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(metadatos);
            return subirArchivo(folderId, "metadatos.json", jsonBytes, "application/json");
        } catch (ErrorExternoException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al serializar metadatos", e);
            throw new ErrorExternoException("Error al procesar metadatos del inmueble");
        }
    }

    /* Headers para JSON */
    private HttpHeaders crearHeadersJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hubSpotToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /* Headers para multipart */
    private HttpHeaders crearHeadersMultipart() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hubSpotToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }
}
