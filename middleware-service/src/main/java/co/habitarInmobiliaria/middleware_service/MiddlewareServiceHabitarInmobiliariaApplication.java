package co.habitarinmobiliaria.middleware_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MiddlewareServiceHabitarInmobiliariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiddlewareServiceHabitarInmobiliariaApplication.class, args);
	}

}
