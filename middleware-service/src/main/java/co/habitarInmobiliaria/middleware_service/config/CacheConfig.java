package co.habitarinmobiliaria.middleware_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableCaching
@EnableScheduling
@Slf4j
public class CacheConfig {

    private CacheManager cacheManager;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(List.of(
                new ConcurrentMapCache("vitrina"),
                new ConcurrentMapCache("inmueble-individual"),
                new ConcurrentMapCache("asesor-info")
        ));
        simpleCacheManager.initializeCaches();
        this.cacheManager = simpleCacheManager;
        return simpleCacheManager;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void evictVitrinaCache() {
        log.debug("Invalidando caché de vitrina");
        evictCache("vitrina");
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void evictInmuebleCache() {
        log.debug("Invalidando caché de inmueble-individual");
        evictCache("inmueble-individual");
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void evictAsesorCache() {
        log.debug("Invalidando caché de asesor-info");
        evictCache("asesor-info");
    }

    private void evictCache(String cacheName) {
        if (cacheManager == null) return;
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}