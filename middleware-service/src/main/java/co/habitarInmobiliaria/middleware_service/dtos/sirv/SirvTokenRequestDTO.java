package co.habitarinmobiliaria.middleware_service.dtos.sirv;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SirvTokenRequestDTO {
    private String clientId;
    private String clientSecret;
}
