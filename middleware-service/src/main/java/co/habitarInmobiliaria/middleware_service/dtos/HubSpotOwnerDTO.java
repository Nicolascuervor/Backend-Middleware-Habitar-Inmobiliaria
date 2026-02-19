package co.habitarinmobiliaria.middleware_service.dtos;

import lombok.Data;

@Data
public class HubSpotOwnerDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
}
