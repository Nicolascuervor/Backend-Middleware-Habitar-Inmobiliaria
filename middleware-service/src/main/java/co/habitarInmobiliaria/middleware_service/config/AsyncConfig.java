package co.habitarinmobiliaria.middleware_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor dedicado para las llamadas paralelas a portales externos.
 * Dimensionado para manejar hasta ~30 llamadas concurrentes por request
 * sin saturar el sistema.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "vitrinaExecutor")
    public Executor vitrinaExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,                              // corePoolSize
                15,                             // maxPoolSize
                60L, TimeUnit.SECONDS,          // keepAliveTime
                new LinkedBlockingQueue<>(50)    // queueCapacity
        );
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }
}
