package cl.radiojugoseo.usuariosroles.controller;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cl.radiojugoseo.usuariosroles.dto.CrearUsuarioInternoRequest;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioResponse;
import cl.radiojugoseo.usuariosroles.dto.UsuarioResumenResponse;
import cl.radiojugoseo.usuariosroles.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import cl.radiojugoseo.usuariosroles.dto.ActualizarUsuarioRequest;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Consultar identidad del usuario autenticado
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
    @GetMapping
    public List<UsuarioResumenResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    // Crear cuentas internas (solo Administrador)
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
