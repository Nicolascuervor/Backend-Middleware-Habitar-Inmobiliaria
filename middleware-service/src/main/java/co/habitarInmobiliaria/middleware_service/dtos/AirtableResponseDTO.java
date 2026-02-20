package co.habitarinmobiliaria.middleware_service.dtos;

import java.util.List;

public record AirtableResponseDTO(List<AirtableRecordDTO> records) {}
