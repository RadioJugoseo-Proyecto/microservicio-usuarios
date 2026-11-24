
package cl.radiojugoseo.usuariosroles.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoderRenovacion;


    public JwtService(
            JwtEncoder jwtEncoder,
            SecretKey clave) {

        this.jwtEncoder = jwtEncoder;

        // Decodificador exclusivo para tokens de renovacion
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Validar expiracion y emisor del JWT
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "radiojugoseo-usuarios"
                )
        );

        this.jwtDecoderRenovacion = decoder;
    }

    public String generarToken(
            Long usuarioId,
            String correo,
            String rol) {

        Instant ahora = Instant.now();

        // Información que contendrá el JWT
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("radiojugoseo-usuarios")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofHours(1)))
                .subject(String.valueOf(usuarioId))
                .claim("correo", correo)
                .claim("rol", rol)
                .build();

        // Algoritmo de firma
        JwsHeader encabezado = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        // Generar el token firmado
        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        encabezado,
                        claims
                )
        ).getTokenValue();
    }

    // Generar un JWT asociado a la version actual de la cuenta
    public String generarToken(
            Long usuarioId,
            String correo,
            String rol,
            Long versionCuenta) {

        Instant ahora = Instant.now();

        // Informacion del usuario y de su sesion
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("radiojugoseo-usuarios")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofHours(1)))
                .subject(String.valueOf(usuarioId))
                .claim("correo", correo)
                .claim("rol", rol)
                .claim("tipo", "acceso")
                .claim("versionCuenta", versionCuenta)
                .build();

        // Firmar el JWT con HS256
        JwsHeader encabezado = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        encabezado,
                        claims
                )
        ).getTokenValue();
    }

    // Generar un JWT exclusivo para renovar la sesion
    public String generarTokenRenovacion(
            Long usuarioId,
            Long versionCuenta) {

        Instant ahora = Instant.now();

        // Datos del token de renovacion
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("radiojugoseo-usuarios")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(Duration.ofDays(7)))
                .subject(String.valueOf(usuarioId))
                .claim("tipo", "renovacion")
                .claim("versionCuenta", versionCuenta)
                .build();

        // Firmar el token con HS256
        JwsHeader encabezado = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        encabezado,
                        claims
                )
        ).getTokenValue();
    }

    // Verificar que el JWT sea un token de renovacion valido
    public Jwt validarTokenRenovacion(String token) {

        // Comprobar firma, emisor y expiracion
        Jwt jwt = jwtDecoderRenovacion.decode(token);

        // Rechazar tokens que no sean de renovacion
        if (!"renovacion".equals(jwt.getClaimAsString("tipo"))) {
            throw new BadJwtException(
                    "El token proporcionado no es de renovacion"
            );
        }

        return jwt;
    }
}
