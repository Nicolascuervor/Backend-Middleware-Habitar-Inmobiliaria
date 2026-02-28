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
            /* Credenciales como Query Params */
            requestTemplate.query("id_company", idCompany);
            requestTemplate.query("wasi_token", wasiToken);

            /* Content-Type */
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
