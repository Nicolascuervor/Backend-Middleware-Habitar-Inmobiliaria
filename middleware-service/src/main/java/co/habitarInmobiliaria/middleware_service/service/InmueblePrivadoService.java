package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.dtos.AirtableCreateRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.CrearInmueblePrivadoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InmueblePrivadoService {

    private final AirtableClient airtableClient;

    @Value("${airtable.token}")
    private String airtableToken;

    @Value("${airtable.base.id}")
    private String baseId;

    @Value("${airtable.table.inmuebles}")
    private String tableName;

    public String crearInmueble(CrearInmueblePrivadoDTO dto) {
        log.info("Iniciando creación de inmueble privado en Airtable: {}", dto.getTitulo());

        /* Mapear datos al formato de Airtable */
        Map<String, Object> fields = new HashMap<>();

        fields.put("Título", dto.getTitulo());
        fields.put("Tipo Negocio", dto.getTipoNegocio());
        fields.put("Precio", dto.getPrecio());

        /* Relaciones */
        fields.put("ID Dueño", dto.getIdDueno());
        fields.put("ID Contacto", dto.getIdContacto());

        if (dto.getValorAdministracion() != null)
            fields.put("Valor Administración", dto.getValorAdministracion());

        fields.put("Ubicación", dto.getUbicacion());
        if (dto.getZona() != null)
            fields.put("Zona", dto.getZona());
        if (dto.getDireccion() != null)
            fields.put("Dirección", dto.getDireccion());
        if (dto.getEstrato() != null)
            fields.put("Estrato", dto.getEstrato());

        fields.put("Tipo Inmueble", dto.getTipoInmueble());
        if (dto.getAreaConstruida() != null)
            fields.put("Área Construida", dto.getAreaConstruida());
        if (dto.getAreaTerreno() != null)
            fields.put("Área Terreno", dto.getAreaTerreno());
        if (dto.getAreaPrivada() != null)
            fields.put("Área Privada", dto.getAreaPrivada());

        fields.put("Habitaciones", dto.getHabitaciones());
        fields.put("Baños", dto.getBanos());
        if (dto.getEstacionamiento() != null)
            fields.put("Estacionamiento", dto.getEstacionamiento());
        if (dto.getPiso() != null)
            fields.put("Piso", dto.getPiso());
        if (dto.getEstadoFisico() != null)
            fields.put("Estado Físico", dto.getEstadoFisico());
        if (dto.getAnioConstruccion() != null)
            fields.put("Año Construcción", dto.getAnioConstruccion());

        fields.put("Descripción", dto.getDescripcion());

        /* Listas de selección múltiple */
        if (dto.getCaracteristicasInternas() != null)
            fields.put("Características Internas", dto.getCaracteristicasInternas());
        if (dto.getCaracteristicasExternas() != null)
            fields.put("Características Externas", dto.getCaracteristicasExternas());

        /* Imágenes en formato Airtable */
        if (dto.getImagenesUrls() != null && !dto.getImagenesUrls().isEmpty()) {
            List<Map<String, String>> imagenesAirtable = new ArrayList<>();
            for (String url : dto.getImagenesUrls()) {
                Map<String, String> imgObj = new HashMap<>();
                imgObj.put("url", url);
                imagenesAirtable.add(imgObj);
            }
            fields.put("Imágenes", imagenesAirtable);
        }

        /* Construir payload para Airtable */
        AirtableCreateRequestDTO.Record record = AirtableCreateRequestDTO.Record.builder().fields(fields).build();

        AirtableCreateRequestDTO request = AirtableCreateRequestDTO.builder()
                .records(List.of(record))
                .typecast(true)
                .build();

        /* Enviar a Airtable */
        String tokenFormateado = "Bearer " + airtableToken;
        JsonNode response = airtableClient.crearRegistro(tokenFormateado, tableName, request);

        /* Extraer y retornar el ID generado */
        if (response != null && response.has("records") && response.get("records").isArray()) {
            String recordId = response.get("records").get(0).get("id").asText();
            log.info("Inmueble guardado exitosamente en Airtable con ID: {}", recordId);
            return recordId;
        }

        throw new RuntimeException("Error al guardar en Airtable: No se recibió un ID válido");
    }
}