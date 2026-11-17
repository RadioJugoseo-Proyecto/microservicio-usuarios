package cl.radiojugoseo.usuariosroles.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    // Consultar la identidad del usuario autenticado
    @GetMapping("/me")
    public Map<String, String> obtenerMiUsuario(
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "id", jwt.getSubject(),
                "correo", jwt.getClaimAsString("correo"),
                "rol", jwt.getClaimAsString("rol")
        );
    }
}
