package co.habitarinmobiliaria.middleware_service.dtos.wasi;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Map;

/* DTO del inmueble Wasi sin campos innecesarios */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WasiInmuebleDTO {

    /* Identificadores y básicos */
    @JsonAlias("id_property")
    private String idProperty;

    private String title;

    @JsonAlias("sale_price_label")
    private String salePriceLabel;

    @JsonAlias("rent_price_label")
    private String rentPriceLabel;

    @JsonAlias("for_sale")
    private String forSale;

    @JsonAlias("for_rent")
    private String forRent;

    @JsonProperty("sale_price")
    private String salePrice;

    @JsonProperty("rent_price")
    private String rentPrice;

    /* Ubicación */
    @JsonAlias("country_label")
    private String countryLabel;

    @JsonAlias("region_label")
    private String regionLabel;

    @JsonAlias("city_label")
    private String cityLabel;

    @JsonAlias("zone_label")
    private String zoneLabel;

    private String address;

    /* Detalles físicos */
    @JsonAlias("property_type_label")
    private String propertyTypeLabel;

    @JsonAlias("built_area")
    private String builtArea;

    @JsonAlias("area")
    private String area;

    @JsonAlias("private_area")
    private String privateArea;

    private String bedrooms;

    private String bathrooms;

    private String garages;

    private String floor;

    private String stratum;

    @JsonAlias("property_condition_label")
    private String propertyConditionLabel;

    @JsonAlias("building_date")
    private String buildingDate;

    @JsonAlias("maintenance_fee")
    private String maintenanceFee;

    @JsonProperty("user_data")
    private com.fasterxml.jackson.databind.JsonNode userData;

    /* Descripción */
    private String observations;

    /* Imagen principal */
    @JsonAlias("main_image")
    private WasiImageDTO mainImage;

    /* Características */
    private JsonNode features;

    /* Galería de imágenes */
    private JsonNode galleries;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WasiImageDTO {
        private String url;
    }

    /* Características internas y externas */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WasiFeaturesDTO {
        private Map<String, String> internal;
        private Map<String, String> external;
    }
}
