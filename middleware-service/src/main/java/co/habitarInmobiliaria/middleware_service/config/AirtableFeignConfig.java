package co.habitarinmobiliaria.middleware_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import feign.Logger;

public class AirtableFeignConfig {

    @Value("${airtable.token}")
    private String airtableToken;

    @Bean
    public RequestInterceptor airtableRequestInterceptor() {
        return requestTemplate -> {
            // Airtable requiere que el token se envíe como un Bearer Token
            requestTemplate.header("Authorization", "Bearer " + airtableToken);
        };
    }

    // --- NUEVO CÓDIGO PARA DEBUG ---
    @Bean
    Logger.Level feignLoggerLevel() {
        // FULL imprimirá los headers (incluyendo el token), el cuerpo y la URL exacta
        return Logger.Level.FULL;
    }
}
