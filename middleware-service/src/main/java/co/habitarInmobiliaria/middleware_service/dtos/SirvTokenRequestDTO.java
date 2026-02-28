package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SirvTokenRequestDTO {
    private String clientId;
    private String clientSecret;
}
