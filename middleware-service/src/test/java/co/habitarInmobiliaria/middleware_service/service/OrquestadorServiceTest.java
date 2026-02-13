package co.habitarInmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.HubSpotClient;
import co.habitarinmobiliaria.middleware_service.client.WasiClient;
import co.habitarinmobiliaria.middleware_service.dtos.HubSpotContactDTO;
import co.habitarinmobiliaria.middleware_service.dtos.VitrinaInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.dtos.WasiInmuebleDTO;
import co.habitarinmobiliaria.middleware_service.service.InmuebleMapperService;
import co.habitarinmobiliaria.middleware_service.service.OrquestadorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
class OrquestadorServiceTest {

    // 1. Los "Dobles" (Dependencias)
    @Mock
    private HubSpotClient hubSpotClient;

    @Mock
    private WasiClient wasiClient;

    @Mock
    private InmuebleMapperService mapperService;

    // 2. El Sujeto de Prueba (Real)
    @InjectMocks
    private OrquestadorService orquestadorService;

    @Test
    @DisplayName("Debe retornar una lista de inmuebles cuando las APIs responden correctamente")
    void testProcesarVitrina_HappyPath() {
        // --- ARRANGE (Preparar el escenario) ---
        String usuarioToken = "token-valido-123";
        String idInmueble = "9773703";
        String urlWasi = "https://buscador.../" + idInmueble;

        // Simulamos respuesta de HubSpot
        HubSpotContactDTO mockContact = new HubSpotContactDTO();
        HubSpotContactDTO.PropertiesDTO props = new HubSpotContactDTO.PropertiesDTO();
        props.setListing1(urlWasi); // Solo llenamos el listing 1
        mockContact.setProperties(props);

        // Simulamos respuesta de Wasi
        WasiInmuebleDTO mockWasiInmueble = new WasiInmuebleDTO();
        mockWasiInmueble.setIdProperty(idInmueble);
        mockWasiInmueble.setTitle("Apartamento de Prueba");

        // Simulamos el DTO final esperado
        VitrinaInmuebleDTO mockVitrinaDTO = VitrinaInmuebleDTO.builder()
                .id(idInmueble)
                .titulo("Apartamento de Prueba")
                .build();

        // Enseñamos a los Mocks qué hacer (Stubbing)
        when(hubSpotClient.obtenerContacto(eq(usuarioToken), anyString())).thenReturn(mockContact);
        when(mapperService.extraerIdDeUrl(urlWasi)).thenReturn(idInmueble);
        when(wasiClient.obtenerInmueblePorId(idInmueble)).thenReturn(mockWasiInmueble);
        when(mapperService.mapToVitrina(mockWasiInmueble)).thenReturn(mockVitrinaDTO);

        // --- ACT (Ejecutar la acción) ---
        List<VitrinaInmuebleDTO> resultado = orquestadorService.procesarVitrina(usuarioToken);

        // --- ASSERT (Verificar resultados) ---
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(1, resultado.size(), "Debería haber 1 inmueble en la lista");
        Assertions.assertEquals("Apartamento de Prueba", resultado.get(0).getTitulo());

        // Verificamos que se llamaron a los servicios (Opcional pero recomendado)
        verify(hubSpotClient, times(1)).obtenerContacto(anyString(), anyString());
        verify(wasiClient, times(1)).obtenerInmueblePorId(anyString());
    }

    @Test
    @DisplayName("Debe ignorar inmuebles fallidos y retornar solo los válidos (Resiliencia)")
    void testProcesarVitrina_ConFallosEnWasi() {
        // --- ARRANGE ---
        String token = "token-resiliencia";
        String urlValida = "url-1";
        String urlFallida = "url-2"; // Esta causará error

        // HubSpot retorna 2 listings
        HubSpotContactDTO contact = new HubSpotContactDTO();
        HubSpotContactDTO.PropertiesDTO props = new HubSpotContactDTO.PropertiesDTO();
        props.setListing1(urlValida);
        props.setListing2(urlFallida);
        contact.setProperties(props);

        // Mapper extrae IDs
        when(hubSpotClient.obtenerContacto(eq(token), anyString())).thenReturn(contact);
        when(mapperService.extraerIdDeUrl(urlValida)).thenReturn("100");
        when(mapperService.extraerIdDeUrl(urlFallida)).thenReturn("200");

        // Wasi: El ID 100 funciona
        WasiInmuebleDTO wasi100 = new WasiInmuebleDTO();
        when(wasiClient.obtenerInmueblePorId("100")).thenReturn(wasi100);
        when(mapperService.mapToVitrina(wasi100)).thenReturn(VitrinaInmuebleDTO.builder().id("100").build());

        // Wasi: El ID 200 explota (Simulamos RuntimeException)
        when(wasiClient.obtenerInmueblePorId("200")).thenThrow(new RuntimeException("API Wasi Timeout"));

        // --- ACT ---
        List<VitrinaInmuebleDTO> resultado = orquestadorService.procesarVitrina(token);

        // --- ASSERT ---
        Assertions.assertEquals(1, resultado.size(), "Debería retornar 1 elemento aunque el segundo falló");
        Assertions.assertEquals("100", resultado.get(0).getId());

        // Verificamos que el sistema intentó llamar a Wasi dos veces, aunque una falló
        verify(wasiClient, times(2)).obtenerInmueblePorId(anyString());
    }
}
