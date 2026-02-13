package co.habitarinmobiliaria.middleware_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ErrorExternoException extends RuntimeException {
    public ErrorExternoException(String mensaje) {
        super(mensaje);
    }
}