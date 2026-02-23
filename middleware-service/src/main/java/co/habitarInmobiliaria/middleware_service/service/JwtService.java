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

    // Genera la llave criptográfica a partir del application.properties
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generarToken(String correoAsesor, String hubspotOwnerId) {
        return Jwts.builder()
                .subject(correoAsesor)
                .claim("ownerId", hubspotOwnerId) // <-- NUEVO: Guardamos el ID dentro del token
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
