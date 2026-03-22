package co.habitarinmobiliaria.middleware_service.service;

import co.habitarinmobiliaria.middleware_service.client.AirtableClient;
import co.habitarinmobiliaria.middleware_service.dtos.airtable.AirtableCreateRequestDTO;
import co.habitarinmobiliaria.middleware_service.dtos.CrearInmueblePrivadoDTO;
import co.habitarinmobiliaria.middleware_service.exception.ErrorExternoException;
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

    private static final String FIELD_RECORDS = "records";

    private final AirtableClient airtableClient;

    @Value("${airtable.token}")
    private String airtableToken;

    @Value("${airtable.table.inmuebles}")
    private String tableName;

    public String crearInmueble(CrearInmueblePrivadoDTO dto) {
        log.info("Iniciando creación de inmueble privado en Airtable: {}", dto.getTitulo());

        /* Mapear datos al formato de Airtable */
        Map<String, Object> fields = new HashMap<>();

        /* Campos obligatorios */
        fields.put("Título", dto.getTitulo());
        fields.put("Tipo Negocio", dto.getTipoNegocio());
        fields.put("Precio", dto.getPrecio());
        fields.put("ID Dueño", dto.getIdDueno());
        fields.put("ID Contacto", dto.getIdContacto());
        fields.put("Ubicación", dto.getUbicacion());
        fields.put("Tipo Inmueble", dto.getTipoInmueble());
        fields.put("Habitaciones", dto.getHabitaciones());
        fields.put("Baños", dto.getBanos());
        fields.put("Descripción", dto.getDescripcion());

        /* Campos opcionales */
        agregarCampoOpcional(fields, "Valor Administración", dto.getValorAdministracion());
        agregarCampoOpcional(fields, "Zona", dto.getZona());
        agregarCampoOpcional(fields, "Dirección", dto.getDireccion());
        agregarCampoOpcional(fields, "Estrato", dto.getEstrato());
        agregarCampoOpcional(fields, "Área Construida", dto.getAreaConstruida());
        agregarCampoOpcional(fields, "Área Terreno", dto.getAreaTerreno());
        agregarCampoOpcional(fields, "Área Privada", dto.getAreaPrivada());
        agregarCampoOpcional(fields, "Estacionamiento", dto.getEstacionamiento());
        agregarCampoOpcional(fields, "Piso", dto.getPiso());
        agregarCampoOpcional(fields, "Estado Físico", dto.getEstadoFisico());
        agregarCampoOpcional(fields, "Año Construcción", dto.getAnioConstruccion());
        agregarCampoOpcional(fields, "Características Internas", dto.getCaracteristicasInternas());
        agregarCampoOpcional(fields, "Características Externas", dto.getCaracteristicasExternas());

        /* Imágenes en formato Airtable */
        agregarImagenes(fields, dto.getImagenesUrls());

        /* Construir payload para Airtable */
        AirtableCreateRequestDTO.Record registro = AirtableCreateRequestDTO.Record.builder().fields(fields).build();

        AirtableCreateRequestDTO request = AirtableCreateRequestDTO.builder()
                .records(List.of(registro))
                .typecast(true)
                .build();

        /* Enviar a Airtable */
        String tokenFormateado = "Bearer " + airtableToken;
        JsonNode response = airtableClient.crearRegistro(tokenFormateado, tableName, request);

        /* Extraer y retornar el ID generado */
        if (response != null && response.has(FIELD_RECORDS) && response.get(FIELD_RECORDS).isArray()) {
            String recordId = response.get(FIELD_RECORDS).get(0).get("id").asText();
            log.info("Inmueble guardado exitosamente en Airtable con ID: {}", recordId);
            return recordId;
        }

        throw new ErrorExternoException("Error al guardar en Airtable: No se recibió un ID válido");
    }

    private void agregarCampoOpcional(Map<String, Object> fields, String key, Object value) {
        if (value != null) {
            fields.put(key, value);
        }
    }

    private void agregarImagenes(Map<String, Object> fields, List<String> imagenesUrls) {
        if (imagenesUrls == null || imagenesUrls.isEmpty()) {
            return;
        }
        List<Map<String, String>> imagenesAirtable = new ArrayList<>();
        for (String url : imagenesUrls) {
            Map<String, String> imgObj = new HashMap<>();
            imgObj.put("url", url);
            imagenesAirtable.add(imgObj);
        }
        fields.put("Imágenes", imagenesAirtable);
    }
}