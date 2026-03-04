package co.habitarinmobiliaria.middleware_service.client;

import co.habitarinmobiliaria.middleware_service.config.WasiFeignConfig;
import co.habitarinmobiliaria.middleware_service.dtos.wasi.WasiInmuebleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/* Cliente Feign para Wasi */
@FeignClient(name = "wasi-client", url = "${wasi.api.url}", configuration = WasiFeignConfig.class)
public interface WasiClient {

    @GetMapping("/property/get/{idProperty}")
    WasiInmuebleDTO obtenerInmueblePorId(@PathVariable("idProperty") String idProperty);
}
