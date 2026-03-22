package co.habitarinmobiliaria.middleware_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("dev") /* Solo activo en desarrollo */
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Middleware Inmobiliario - Habitar")
                        .version("1.0.0")
                        .description("Documentación del Middleware para la orquestación entre HubSpot y Wasi. " +
                                "Diseñado para servir al Frontend de Adulto Mayor.")
                        .contact(new Contact()
                                .name("Equipo de Ingeniería - IV Semestre")
                                .email("dev-team@habitar.co"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
