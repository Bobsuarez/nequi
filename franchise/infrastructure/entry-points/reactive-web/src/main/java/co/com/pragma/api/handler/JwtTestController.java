package co.com.pragma.api.handler;

import co.com.pragma.config.security.jwt.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/jwt-test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @GetMapping("/generate-token")
    public Mono<Map<String, String>> generateToken(@RequestParam(name= "role" ,defaultValue = "ADMIN") String role) {

        String token = Jwts.builder()
                .subject("test-user")
                .claim("roles", List.of(role))
                .audience().add(jwtProperties.getAudience()).and()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration() * 1000))
                .signWith(getSigningKey())
                .compact();

        return Mono.just(Map.of(
                "token", token,
                "role", role,
                "bearer", "Bearer " + token
        ));
    }

    @GetMapping("/generate-invalid-token")
    public Mono<Map<String, String>> generateInvalidToken() {
        return generateToken("CLIENT"); // Rol no autorizado para REGISTER_LOAN_CLIENT
    }

    @GetMapping("/generate-valid-token")
    public Mono<Map<String, String>> generateValidToken() {
        return generateToken("PERSON"); // Rol autorizado para REGISTER_LOAN_CLIENT
    }
}