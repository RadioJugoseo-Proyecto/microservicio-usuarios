
package cl.radiojugoseo.usuariosroles.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.radiojugoseo.usuariosroles.dto.LoginRequest;
import cl.radiojugoseo.usuariosroles.dto.LoginResponse;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioRequest;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioResponse;
import cl.radiojugoseo.usuariosroles.dto.RenovarTokenRequest;
import cl.radiojugoseo.usuariosroles.service.UsuarioService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Registrar una nueva cuenta Cliente
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cliente registrado correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = RegistroUsuarioResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de registro invalidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Correo o RUN ya registrado",
            content = @Content
        )
    })
    @PostMapping("/registro")
    public ResponseEntity<RegistroUsuarioResponse> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request) {

        RegistroUsuarioResponse respuesta =
                usuarioService.registrarCliente(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // Iniciar sesion y obtener ambos JWT
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Inicio de sesion exitoso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = LoginResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de inicio de sesion invalidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales incorrectas o cuenta inactiva",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse respuesta =
                usuarioService.iniciarSesion(request);

        return ResponseEntity.ok(respuesta);
    }

    // Renovar la sesion con un token de renovacion
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "JWT de acceso renovado correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = LoginResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Token de renovacion no proporcionado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token invalido, expirado o sesion revocada",
            content = @Content
        )
    })
    @PostMapping("/renovar")
    public ResponseEntity<LoginResponse> renovarToken(
            @Valid @RequestBody RenovarTokenRequest request) {

        LoginResponse respuesta =
                usuarioService.renovarSesion(request);

        return ResponseEntity.ok(respuesta);
    }
}
