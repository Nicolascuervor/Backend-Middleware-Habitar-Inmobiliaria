package co.habitarinmobiliaria.middleware_service.controller;

import co.habitarinmobiliaria.middleware_service.dtos.AuthResponseDTO;
import co.habitarinmobiliaria.middleware_service.dtos.LoginRequestDTO;
import co.habitarinmobiliaria.middleware_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login de Asesor", description = "Valida credenciales contra Airtable y retorna un JWT")
    public ResponseEntity<AuthResponseDTO> login(@jakarta.validation.Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
