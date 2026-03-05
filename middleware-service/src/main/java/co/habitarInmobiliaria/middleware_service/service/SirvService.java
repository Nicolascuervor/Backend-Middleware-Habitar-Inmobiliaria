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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SirvService {

    private final SirvClient sirvClient;

    @Value("${sirv.api.client-id}")
    private String clientId;

    @Value("${sirv.api.client-secret}")
    private String clientSecret;

    @Value("${sirv.cuenta.dominio}")
    private String dominioSirv;

    public String subirImagen(MultipartFile archivo) {
        try {
            log.info("Iniciando subida de imagen a Sirv: {}", archivo.getOriginalFilename());

            // 1. Obtener Token
            SirvTokenRequestDTO authRequest = SirvTokenRequestDTO.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            SirvTokenResponseDTO authResponse = sirvClient.obtenerToken(authRequest);
            String tokenHeader = "Bearer " + authResponse.getToken();

            // 2. Generar un nombre único para evitar sobreescribir imágenes (Ej:
            // /inmuebles/123e4567-e89b...jpg)
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreUnico = UUID.randomUUID().toString() + extension;
            String rutaDestino = "/inmuebles/" + nombreUnico; // Sirv creará la carpeta automáticamente

            // 3. Enviar a Sirv
            sirvClient.subirArchivo(
                    tokenHeader,
                    archivo.getContentType(), // Ej: "image/jpeg"
                    rutaDestino,
                    archivo.getBytes());

            // 4. Construir y devolver la URL pública
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

    // Método utilitario
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
        }
        return ".jpg"; // Por defecto
    }
}
