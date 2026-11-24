package cl.radiojugoseo.usuariosroles.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI radioJugoseoOpenApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Radio Jugoseo - Microservicio de Usuarios")
                        .version("v1")
                        .description(
                                "API REST para gestion de usuarios, " +
                                "autenticacion JWT y autorizacion por roles."
                        )
                )
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Ingresar un JWT de acceso valido"
                                        )
                        )
                );
    }
}