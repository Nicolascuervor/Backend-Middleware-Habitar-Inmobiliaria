package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.SirvClient;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import co.habitarinmobiliaria.middleware_service.dtos.sirv.SirvTokenRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.sirv.SirvTokenResponseDTO;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SirvService {

    private final SirvClient sirvClient;

    /* Extensiones y tipos MIME permitidos */
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANO_MAXIMO = 10 * 1024 * 1024; /* 10 MB */

    @Value("${sirv.api.client-id}")
    private String clientId;

    @Value("${sirv.api.client-secret}")
    private String clientSecret;

    @Value("${sirv.cuenta.dominio}")
    private String dominioSirv;

    public String subirImagen(MultipartFile archivo) {
        /* Validar archivo */
        validarArchivo(archivo);

        try {
            String nombreSeguro = archivo.getOriginalFilename() != null
                    ? archivo.getOriginalFilename().replaceAll("[\\r\\n\\t]", "_")
                    : "sin_nombre";
            log.info("Iniciando subida de imagen a Sirv: {}", nombreSeguro);

            /* Obtener Token */
            SirvTokenRequestDTO authRequest = SirvTokenRequestDTO.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            SirvTokenResponseDTO authResponse = sirvClient.obtenerToken(authRequest);
            String tokenHeader = "Bearer " + authResponse.getToken();

            /* Generar nombre único con extensión validada */
            String extension = obtenerExtension(archivo.getOriginalFilename()).toLowerCase();
            String nombreUnico = UUID.randomUUID().toString() + extension;
            String rutaDestino = "/inmuebles/" + nombreUnico;

            /* Enviar a Sirv */
            sirvClient.subirArchivo(
                    tokenHeader,
                    archivo.getContentType(),
                    rutaDestino,
                    archivo.getBytes());

            /* Construir y devolver la URL pública */
            String urlPublica = dominioSirv + rutaDestino;
            log.info("Imagen subida exitosamente. URL: {}", urlPublica);

            return urlPublica;

        } catch (IOException e) {
            log.error("Error al leer los bytes del archivo", e);
            throw new ErrorExternoException("Error al procesar el archivo de imagen");
        } catch (Exception e) {
            log.error("Error en la comunicación con Sirv", e);
            throw new ErrorExternoException("Error al subir la imagen a la nube");
        }
    }

    /* Validación de archivo en tres capas */
    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo (10 MB)");
        }

        String extension = obtenerExtension(archivo.getOriginalFilename()).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida. Solo: " + EXTENSIONES_PERMITIDAS);
        }

        if (archivo.getContentType() == null || !CONTENT_TYPES_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido");
        }
    }

    // Método utilitario
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
        }
        return ".jpg"; // Por defecto
    }
}
