
package cl.radiojugoseo.usuariosroles.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.radiojugoseo.usuariosroles.dto.ActualizarUsuarioRequest;
import cl.radiojugoseo.usuariosroles.dto.CrearUsuarioInternoRequest;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioResponse;
import cl.radiojugoseo.usuariosroles.dto.UsuarioResumenResponse;
import cl.radiojugoseo.usuariosroles.service.UsuarioService;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Consultar identidad del usuario autenticado
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Identidad del usuario autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado",
            content = @Content
        )
    })
    @GetMapping("/me")
    public Map<String, String> obtenerMiUsuario(
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "id", jwt.getSubject(),
                "correo", jwt.getClaimAsString("correo"),
                "rol", jwt.getClaimAsString("rol")
        );
    }

    // Listar usuarios registrados (solo Administrador)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado de usuarios obtenido correctamente",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(
                        implementation = UsuarioResumenResponse.class
                    )
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos de Administrador",
            content = @Content
        )
    })
    @GetMapping
    public List<UsuarioResumenResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // Crear cuentas internas (solo Administrador)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = RegistroUsuarioResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de usuario invalidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos de Administrador",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El correo ya esta registrado",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<RegistroUsuarioResponse> crearUsuarioInterno(
            @Valid @RequestBody CrearUsuarioInternoRequest request) {

        RegistroUsuarioResponse respuesta =
                usuarioService.crearUsuarioInterno(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // Actualizar un usuario (solo Administrador)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario actualizado correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = UsuarioResumenResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de actualizacion invalidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Operacion no permitida",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public UsuarioResumenResponse actualizarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActualizarUsuarioRequest request) {

        return usuarioService.actualizarUsuario(
                id,
                jwt.getSubject(),
                request
        );
    }

    // Eliminar un usuario (solo Administrador)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Usuario eliminado correctamente",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Operacion no permitida",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        usuarioService.eliminarUsuario(
                id,
                jwt.getSubject()
        );

        return ResponseEntity.noContent().build();
    }
}
