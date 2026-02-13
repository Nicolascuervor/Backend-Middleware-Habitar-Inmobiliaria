package co.habitarinmobiliaria.middleware_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class HubSpotFeignConfig {

    @Value("${hubspot.token}") // Token de Private App
    private String hubSpotToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + hubSpotToken);
        };
    }
}