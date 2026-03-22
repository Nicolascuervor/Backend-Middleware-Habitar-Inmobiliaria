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
        return requestTemplate ->
                /* Bearer Token para Airtable */
                requestTemplate.header("Authorization", "Bearer " + airtableToken);
    }

    /* Nivel de logging Feign */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; /* Solo método, URL y status */
    }
}
