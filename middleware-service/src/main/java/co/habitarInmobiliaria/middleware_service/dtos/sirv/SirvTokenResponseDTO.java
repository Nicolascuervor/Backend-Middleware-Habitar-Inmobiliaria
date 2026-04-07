package co.habitarinmobiliaria.middleware_service.dtos.sirv;

import lombok.Data;

@Data
public class SirvTokenResponseDTO {
    private String token;
    private int expiresIn;
}
