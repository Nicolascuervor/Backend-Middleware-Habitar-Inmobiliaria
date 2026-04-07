package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.dtos.hubspot.HubSpotOwnerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Servicio dedicado para cachear datos de asesores.
 * Se separa de OrquestadorService para evitar el problema de
 * auto-invocación de proxies Spring (@Cacheable no funciona
 * cuando un método de la misma clase llama a otro cacheado).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsesorCacheService {

    private final HubSpotClient hubSpotClient;
    @Cacheable(value = "asesor-info", key = "#ownerId")
    public HubSpotOwnerDTO obtenerAsesor(String ownerId) {
        log.info("Cache MISS — Consultando HubSpot para asesor: {}", ownerId);
        return hubSpotClient.obtenerAsesor(ownerId);
    }
}
