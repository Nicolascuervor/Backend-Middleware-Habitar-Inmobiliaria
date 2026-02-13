package co.habitarinmobiliaria.middleware_service.client;


import co.habitarinmobiliaria.middleware_service.config.WasiFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.WasiInmuebleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "url" debe estar en application.properties como wasi.api.url=https://api.wasi.co/v1
@FeignClient(name = "wasi-client", url = "${wasi.api.url}", configuration = WasiFeignConfig.class)
public interface WasiClient {

    /**
     * CORRECCIÓN: El endpoint correcto en Wasi es /property/get/{id}
     * No se preocupen por ?id_company y ?wasi_token, el Interceptor (WasiFeignConfig)
     * ya los está inyectando en el Header o Query param automáticamente.
     */
    @GetMapping("/property/get/{idProperty}")
    WasiInmuebleDTO obtenerInmueblePorId(@PathVariable("idProperty") String idProperty);
}
