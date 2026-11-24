package cl.radiojugoseo.usuariosroles.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import cl.radiojugoseo.usuariosroles.repository.UsuarioRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${JWT_SECRET}") String claveBase64) {

        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(claveBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET debe estar codificada en Base64",
                    ex
            );
        }

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe contener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey clave) {
        return NimbusJwtEncoder
                .withSecretKey(clave)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey clave,
            UsuarioRepository usuarioRepository) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Respuesta para un JWT que ya no es válido
        OAuth2TokenValidatorResult tokenInvalido =
                OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "La sesion no esta vigente",
                                null
                        )
                );

        // Comprobar el estado actual de la cuenta en MySQL
        OAuth2TokenValidator<Jwt> validarCuenta = jwt -> {

            // Solo aceptar tokens de acceso
            if (!"acceso".equals(jwt.getClaimAsString("tipo"))) {
                return tokenInvalido;
            }

            // Obtener el ID del usuario
            Long usuarioId;

            try {
                usuarioId = Long.valueOf(jwt.getSubject());
            } catch (NumberFormatException ex) {
                return tokenInvalido;
            }

            // Obtener la version incluida en el JWT
            Object versionToken = jwt.getClaim("versionCuenta");

            if (!(versionToken instanceof Number numeroVersion)) {
                return tokenInvalido;
            }

            // Buscar la cuenta y comprobar su estado
            boolean cuentaValida = usuarioRepository.findById(usuarioId)
                    .filter(usuario ->
                            Boolean.TRUE.equals(usuario.getActivo())
                    )
                    .filter(usuario ->
                            usuario.getVersionCuenta() != null
                            && usuario.getVersionCuenta().longValue()
                                    == numeroVersion.longValue()
                    )
                    .isPresent();

            return cuentaValida
                    ? OAuth2TokenValidatorResult.success()
                    : tokenInvalido;
        };

        // Conservar validaciones normales y agregar la de MySQL
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer(
                                "radiojugoseo-usuarios"
                        ),
                        validarCuenta
                )
        );

        return decoder;
    }
}
