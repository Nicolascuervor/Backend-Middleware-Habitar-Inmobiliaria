package co.habitarinmobiliaria.middleware_service.constans;

public final class HubSpotConstants {

    private HubSpotConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }

    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String OWNER_ID = "hubspot_owner_id";
    public static final String LISTING_PREFIX = "listing_";


    public static final String LISTINGS_ALQUILER_DATA = "listings_alquiler_data";
    public static final String LISTINGS_VENTA_DATA = "listings_venta_data";
}
