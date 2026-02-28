package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Data;

@Data
public class SirvTokenResponseDTO {
    private String token;
    private int expiresIn;
}
