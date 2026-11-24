package cl.radiojugoseo.usuariosroles.config;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        // Interpretar el campo "rol" del JWT
        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter convertidor =
                new JwtAuthenticationConverter();

        convertidor.setJwtGrantedAuthoritiesConverter(roles);

        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .authorizeHttpRequests(auth -> auth

                .dispatcherTypeMatchers(
                    DispatcherType.ERROR
                ).permitAll()

                // Rutas de autenticacion que no requieren JWT de acceso
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/registro",
                    "/api/v1/auth/login",
                    "/api/v1/auth/renovar"
                ).permitAll()

                // Permitir consultar la documentacion Swagger/OpenAPI
                .requestMatchers(
                    HttpMethod.GET,
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**"
                ).permitAll()

                // Solo el Administrador puede listar usuarios
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/usuarios"
                ).hasRole("Administrador")

                // Solo el Administrador puede crear cuentas internas
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/usuarios"
                ).hasRole("Administrador")

                // Solo el Administrador puede actualizar usuarios
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/usuarios/{id}"
                ).hasRole("Administrador")

                // Solo el Administrador puede eliminar usuarios
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/usuarios/{id}"
                ).hasRole("Administrador")

                .anyRequest().authenticated()
            )

            // Validar JWT y reconocer el rol del usuario
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(convertidor)
                )
            );

        return http.build();
    }
}
