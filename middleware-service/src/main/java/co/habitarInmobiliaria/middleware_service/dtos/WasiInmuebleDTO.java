package co.habitarinmobiliaria.middleware_service.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Representación cruda del inmueble tal cual viene de Wasi.
 * Se usa @JsonIgnoreProperties para ignorar los 100 campos que no usaremos.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WasiInmuebleDTO {

    @JsonAlias("id_property")
    private String idProperty;

    private String title;

    @JsonAlias("sale_price_label")
    private String salePriceLabel;

    @JsonAlias("rent_price_label")
    private String rentPriceLabel;

    // Lógica para saber si es venta o arriendo
    @JsonAlias("for_sale")
    private String forSale;

    @JsonAlias("for_rent")
    private String forRent;

    // Ubicación compuesta
    @JsonAlias("city_label")
    private String cityLabel;

    @JsonAlias("zone_label")
    private String zoneLabel;

    // Imagen principal (Objeto anidado)
    @JsonAlias("main_image")
    private WasiImageDTO mainImage;

    // Descripción HTML (Cuidado: habrá que limpiarla para el abuelo)
    private String observations;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WasiImageDTO {
        private String url;
    }
}
