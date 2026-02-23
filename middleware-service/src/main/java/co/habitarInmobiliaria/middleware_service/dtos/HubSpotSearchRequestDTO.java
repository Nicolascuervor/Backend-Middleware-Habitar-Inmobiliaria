package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HubSpotSearchRequestDTO {
    private List<FilterGroup> filterGroups;
    private List<String> properties;

    @Data
    @Builder
    public static class FilterGroup {
        private List<Filter> filters;
    }

    @Data
    @Builder
    public static class Filter {
        private String propertyName;
        private String operator; // Ej: "EQ" para Equals
        private String value;
    }
}
