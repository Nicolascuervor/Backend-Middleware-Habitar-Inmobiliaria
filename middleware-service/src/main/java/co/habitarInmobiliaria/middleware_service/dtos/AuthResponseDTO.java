package co.habitarinmobiliaria.middleware_service.dtos;


public record AuthResponseDTO(
        String token,
        String nombreAsesor,
        String correo
) {}
