package co.habitarinmobiliaria.middleware_service.exception;

import co.habitarinmobiliaria.middleware_service.dtos.ErrorResponseDTO;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

@Hidden
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja nuestras excepciones personalizadas de "No Encontrado".
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Maneja errores específicos de Feign (cuando Wasi/HubSpot responden con error).
     * Esto evita que el frontend reciba un "500 Internal Server Error" genérico.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(FeignException ex, HttpServletRequest request) {
        // Extraemos el status real que devolvió la API externa (ej. 401, 404, 500)
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;

        String mensaje = "Error en comunicación con servicio externo: " + ex.getMessage();
        log.error("Error Feign detectado: status={} body={}", ex.status(), ex.contentUTF8());

        return construirRespuesta(status, mensaje, request);
    }

    /**
     * "Catch-all": Maneja cualquier otro error no previsto (NullPointer, etc.)
     * Es el último recurso para no romper la app.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado no controlado: ", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno inesperado. Contacte soporte.", request);
    }

    // Método privado para construir el DTO de forma uniforme
    private ResponseEntity<ErrorResponseDTO> construirRespuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        // Generamos un Trace ID si no existe uno (Para facilitar soporte)
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
                .traceId(traceId)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorDTO, status);
    }
}
