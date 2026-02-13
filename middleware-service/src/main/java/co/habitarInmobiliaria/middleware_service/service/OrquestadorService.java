package co.habitarinmobiliaria.middleware_service.service;


import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.client.WasiClient;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.WasiInmuebleDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor // Inyección de dependencias por constructor (Lombok)
@Slf4j
public class OrquestadorService {

    private final HubSpotClient hubSpotClient;
    private final WasiClient wasiClient;
    private final InmuebleMapperService mapperService;

    public List<VitrinaInmuebleDTO> procesarVitrina(String usuarioToken) {
        log.info("Iniciando orquestación para usuario: {}", usuarioToken);

        // 1. Obtener datos de HubSpot
        // Solicitamos explícitamente las propiedades que necesitamos
        String propiedadesSolicitadas = "firstname,listing_1,listing_2,listing_3,listing_4,listing_5";

        HubSpotContactDTO contacto = hubSpotClient.obtenerContacto(usuarioToken, propiedadesSolicitadas);

        if (contacto == null || contacto.getProperties() == null) {
            log.warn("Usuario no encontrado o sin propiedades: {}", usuarioToken);
            return new ArrayList<>();
        }

        // 2. Extraer URLs de los listings (ignorando nulos)
        HubSpotContactDTO.PropertiesDTO props = contacto.getProperties();
        List<String> urlsListings = Stream.of(
                props.getListing1(),
                props.getListing2(),
                props.getListing3(),
                props.getListing4(),
                props.getListing5()
        ).filter(Objects::nonNull).collect(Collectors.toList());

        List<VitrinaInmuebleDTO> vitrinaFinal = new ArrayList<>();

        // 3. Iterar y consultar Wasi
        for (String url : urlsListings) {
            String inmuebleId = mapperService.extraerIdDeUrl(url);

            if (inmuebleId != null) {
                try {
                    // Llamada a API Wasi
                    WasiInmuebleDTO inmuebleRaw = wasiClient.obtenerInmueblePorId(inmuebleId);

                    // Transformación a DTO Limpio
                    if (inmuebleRaw != null) {
                        vitrinaFinal.add(mapperService.mapToVitrina(inmuebleRaw));
                    }
                } catch (Exception e) {
                    // Resiliencia: Si falla un inmueble, logueamos y seguimos con el siguiente
                    log.error("Error obteniendo inmueble {}: {}", inmuebleId, e.getMessage());
                }
            }
        }

        return vitrinaFinal;
    }
}