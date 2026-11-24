package cl.radiojugoseo.usuariosroles.controller;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import cl.radiojugoseo.usuariosroles.config.SecurityConfig;
import cl.radiojugoseo.usuariosroles.service.UsuarioService;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // PRUEBA 1: Sin JWT no se puede listar usuarios
    @Test
    void rechazaSolicitudSinAutenticacion() throws Exception {

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    // PRUEBA 2: Un Locutor no puede administrar usuarios
    @Test
    void locutorNoPuedeAdministrarUsuarios() throws Exception {

        var locutor = jwt().authorities(
                new SimpleGrantedAuthority("ROLE_Locutor")
        );

        mockMvc.perform(
                get("/api/v1/usuarios").with(locutor)
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                post("/api/v1/usuarios")
                        .with(locutor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                put("/api/v1/usuarios/7")
                        .with(locutor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                delete("/api/v1/usuarios/7").with(locutor)
        ).andExpect(status().isForbidden());
    }

    // PRUEBA 3: Un Administrador puede listar usuarios
    @Test
    void administradorPuedeListarUsuarios() throws Exception {

        when(usuarioService.listarUsuarios())
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/usuarios")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_Administrador"
                                )
                        ))
        )
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

        verify(usuarioService).listarUsuarios();
    }
}