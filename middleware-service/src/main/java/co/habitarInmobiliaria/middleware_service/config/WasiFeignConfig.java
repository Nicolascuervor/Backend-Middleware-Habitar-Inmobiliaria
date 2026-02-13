package co.habitarinmobiliaria.middleware_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class WasiFeignConfig {

    @Value("${wasi.id.company}")
    private String idCompany;

    @Value("${wasi.token}")
    private String wasiToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // CAMBIO: Enviar credenciales como Query Params (?key=val) en lugar de Headers
            requestTemplate.query("id_company", idCompany);
            requestTemplate.query("wasi_token", wasiToken);

            // Mantenemos el Content-Type por si acaso
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
