package co.habitarinmobiliaria.middleware_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * Configuración de caché en memoria para el middleware.
 * Usa ConcurrentMapCache (sin infraestructura adicional).
 *
 * Caches:
 * - "vitrina"             → TTL 5 min (datos de vitrina completa por token)
 * - "inmueble-individual" → TTL 10 min (datos de inmueble por ID)
 * - "asesor-info"         → TTL 30 min (datos estáticos de asesor por ownerId)
 */
@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    @Autowired
    private CacheManager cacheManager;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new ConcurrentMapCache("vitrina"),
                new ConcurrentMapCache("inmueble-individual"),
                new ConcurrentMapCache("asesor-info")
        ));
        return cacheManager;
    }

    /* Invalidación programada — simula TTL para ConcurrentMapCache */

    @Scheduled(fixedRate = 5 * 60 * 1000) // cada 5 minutos
    public void evictVitrinaCache() {
        log.debug("Invalidando caché de vitrina");
        evictCache("vitrina");
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // cada 10 minutos
    public void evictInmuebleCache() {
        log.debug("Invalidando caché de inmueble-individual");
        evictCache("inmueble-individual");
    }

    @Scheduled(fixedRate = 30 * 60 * 1000) // cada 30 minutos
    public void evictAsesorCache() {
        log.debug("Invalidando caché de asesor-info");
        evictCache("asesor-info");
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
