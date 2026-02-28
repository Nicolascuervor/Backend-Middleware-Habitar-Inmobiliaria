package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AirtableCreateRequestDTO {

    private List<Record> records;

    private Boolean typecast;

    @Data
    @Builder
    public static class Record {
        /* Columnas de la tabla Airtable */
        private Map<String, Object> fields;
    }
}
