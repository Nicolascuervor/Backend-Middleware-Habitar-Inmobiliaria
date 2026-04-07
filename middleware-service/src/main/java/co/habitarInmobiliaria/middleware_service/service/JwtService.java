package co.habitarinmobiliaria.middleware_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /* Validar configuración de JWT al arrancar */
    @jakarta.annotation.PostConstruct
    public void validarConfiguracion() {
        if (secretKey == null || secretKey.isBlank() || secretKey.length() < 64) {
            throw new IllegalStateException(
                    "JWT_SECRET_KEY no configurada o demasiado corta. Mínimo 64 caracteres.");
        }
    }

    /* Generar llave criptográfica */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generarToken(String correoAsesor, String hubspotOwnerId) {
        return Jwts.builder()
                .subject(correoAsesor)
                .claim("ownerId", hubspotOwnerId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extraerOwnerId(String token) {
        return extraerTodosLosClaims(token).get("ownerId", String.class);
    }

    public String extraerCorreo(String token) {
        return extraerTodosLosClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, String correoAsesor) {
        final String correoExtraido = extraerCorreo(token);
        return (correoExtraido.equals(correoAsesor)) && !esTokenExpirado(token);
    }

    private boolean esTokenExpirado(String token) {
        return extraerTodosLosClaims(token).getExpiration().before(new Date());
    }

    private Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
