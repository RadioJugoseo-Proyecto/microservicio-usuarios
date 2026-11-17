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
import cl.radiojugoseo.usuariosroles.service.UsuarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Registro de nuevos clientes
    @PostMapping("/registro")
    public ResponseEntity<RegistroUsuarioResponse> registrar(
            @Valid @RequestBody RegistroUsuarioRequest request) {

        RegistroUsuarioResponse respuesta =
                usuarioService.registrarCliente(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // Inicio de sesión
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse respuesta =
                usuarioService.iniciarSesion(request);

        return ResponseEntity.ok(respuesta);
    }
}
