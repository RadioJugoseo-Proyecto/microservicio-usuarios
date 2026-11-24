package cl.radiojugoseo.usuariosroles.service;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import cl.radiojugoseo.usuariosroles.dto.LoginRequest;
import cl.radiojugoseo.usuariosroles.entity.Usuario;
import cl.radiojugoseo.usuariosroles.repository.RolRepository;
import cl.radiojugoseo.usuariosroles.repository.UsuarioRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import cl.radiojugoseo.usuariosroles.dto.LoginResponse;
import cl.radiojugoseo.usuariosroles.entity.Rol;
import static org.mockito.Mockito.verify;
import cl.radiojugoseo.usuariosroles.dto.RenovarTokenRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import cl.radiojugoseo.usuariosroles.dto.ActualizarUsuarioRequest;
import java.util.List;
import cl.radiojugoseo.usuariosroles.dto.UsuarioResumenResponse;
import cl.radiojugoseo.usuariosroles.dto.CrearUsuarioInternoRequest;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioResponse;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.any;

class UsuarioServiceTest {

    // Dependencias simuladas con Mockito
    private final UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    private final RolRepository rolRepository =
            mock(RolRepository.class);

    private final PasswordEncoder passwordEncoder =
            mock(PasswordEncoder.class);

    private final JwtService jwtService =
            mock(JwtService.class);

    // Servicio que vamos a probar
    private final UsuarioService usuarioService =
            new UsuarioService(
                    usuarioRepository,
                    rolRepository,
                    passwordEncoder,
                    jwtService
            );

    @Test
    void rechazaCorreoNoRegistrado() {

        LoginRequest request = new LoginRequest(
                "inexistente@gmail.com",
                "Password123"
        );

        when(usuarioRepository.findByCorreoIgnoreCase(
                "inexistente@gmail.com"
        )).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void rechazaPasswordIncorrecta() {

        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        usuario.setPasswordHash("hashDePrueba");

        when(usuarioRepository.findByCorreoIgnoreCase(
                "cliente@gmail.com"
        )).thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                "Incorrecta123",
                "hashDePrueba"
        )).thenReturn(false);

        LoginRequest request = new LoginRequest(
                "cliente@gmail.com",
                "Incorrecta123"
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void rechazaCuentaInactiva() {

        Usuario usuario = new Usuario();
        usuario.setActivo(false);

        when(usuarioRepository.findByCorreoIgnoreCase(
                "cliente@gmail.com"
        )).thenReturn(Optional.of(usuario));

        LoginRequest request = new LoginRequest(
                "cliente@gmail.com",
                "Password123"
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.iniciarSesion(request)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void iniciaSesionConCredencialesValidas() {

        // Preparar un usuario activo de prueba
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombre("Maria");
        usuario.setCorreo("maria@gmail.com");
        usuario.setPasswordHash("hashDePrueba");
        usuario.setActivo(true);
        usuario.setVersionCuenta(0L);
        usuario.setRol(new Rol("Cliente"));

        // Simular que el usuario existe en MySQL
        when(usuarioRepository.findByCorreoIgnoreCase(
                "maria@gmail.com"
        )).thenReturn(Optional.of(usuario));

        // Simular que la contraseña es correcta
        when(passwordEncoder.matches(
                "Password123",
                "hashDePrueba"
        )).thenReturn(true);

        // Simular la generacion de ambos JWT
        when(jwtService.generarToken(
                5L,
                "maria@gmail.com",
                "Cliente",
                0L
        )).thenReturn("jwtAccesoSimulado");

        when(jwtService.generarTokenRenovacion(
                5L,
                0L
        )).thenReturn("jwtRenovacionSimulado");

        // Ejecutar el inicio de sesion
        LoginResponse respuesta = usuarioService.iniciarSesion(
                new LoginRequest(
                        "maria@gmail.com",
                        "Password123"
                )
        );

        // Comprobar los datos devueltos
        assertEquals("jwtAccesoSimulado", respuesta.token());
        assertEquals("jwtRenovacionSimulado", respuesta.tokenRenovacion());
        assertEquals("Bearer", respuesta.tipoToken());
        assertEquals(5L, respuesta.id());
        assertEquals("Maria", respuesta.nombre());
        assertEquals("maria@gmail.com", respuesta.correo());
        assertEquals("Cliente", respuesta.rol());

        // Comprobar que se solicitaron ambos tokens
        verify(jwtService).generarToken(
                5L, "maria@gmail.com", "Cliente", 0L
        );

        verify(jwtService).generarTokenRenovacion(5L, 0L);
    }

    @Test
    void rechazaRenovacionConVersionDeCuentaAntigua() {

        // Token de renovacion utilizado en la prueba
        String tokenRenovacion = "tokenRenovacionAntiguo";

        // Simular un JWT valido, pero con version de cuenta 2
        Jwt jwt = mock(Jwt.class);

        when(jwtService.validarTokenRenovacion(
                tokenRenovacion
        )).thenReturn(jwt);

        when(jwt.getSubject()).thenReturn("5");

        when(jwt.getClaim(
                "versionCuenta"
        )).thenReturn(2L);

        // En MySQL la cuenta ya tiene una version mas reciente
        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        usuario.setVersionCuenta(3L);

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(usuario));

        // Intentar renovar la sesion con el token antiguo
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.renovarSesion(
                        new RenovarTokenRequest(tokenRenovacion)
                )
        );

        // El backend debe rechazar la renovacion
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );

        // Comprobar que no se generaron nuevos JWT
        verify(jwtService).validarTokenRenovacion(
                tokenRenovacion
        );

        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void renuevaSesionConTokenValido() {

        // Token de renovacion recibido
        String tokenRenovacion = "tokenRenovacionValido";

        // Simular un JWT de renovacion valido
        Jwt jwt = mock(Jwt.class);

        when(jwtService.validarTokenRenovacion(
                tokenRenovacion
        )).thenReturn(jwt);

        when(jwt.getSubject()).thenReturn("5");

        when(jwt.getClaim(
                "versionCuenta"
        )).thenReturn(3L);

        // Simular el usuario actual en MySQL
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombre("Maria");
        usuario.setCorreo("maria@gmail.com");
        usuario.setActivo(true);
        usuario.setVersionCuenta(3L);
        usuario.setRol(new Rol("Cliente"));

        when(usuarioRepository.findById(5L))
                .thenReturn(Optional.of(usuario));

        // Simular la generacion de un nuevo JWT de acceso
        when(jwtService.generarToken(
                5L,
                "maria@gmail.com",
                "Cliente",
                3L
        )).thenReturn("nuevoJwtAcceso");

        // Ejecutar la renovacion
        LoginResponse respuesta = usuarioService.renovarSesion(
                new RenovarTokenRequest(tokenRenovacion)
        );

        // Comprobar el nuevo token de acceso
        assertEquals("nuevoJwtAcceso", respuesta.token());

        // El token de renovacion original se conserva
        assertEquals(
                tokenRenovacion,
                respuesta.tokenRenovacion()
        );

        // Comprobar los datos de la sesion
        assertEquals("Bearer", respuesta.tipoToken());
        assertEquals(5L, respuesta.id());
        assertEquals("Maria", respuesta.nombre());
        assertEquals("maria@gmail.com", respuesta.correo());
        assertEquals("Cliente", respuesta.rol());

        // Comprobar las llamadas al servicio JWT
        verify(jwtService).validarTokenRenovacion(
                tokenRenovacion
        );

        verify(jwtService).generarToken(
                5L,
                "maria@gmail.com",
                "Cliente",
                3L
        );

        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void impideQueAdministradorElimineSuPropiaCuenta() {

        // ID del usuario que se intenta eliminar
        Long usuarioId = 5L;

        // ID del Administrador autenticado
        String administradorId = "5";

        // Intentar eliminar la propia cuenta
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.eliminarUsuario(
                        usuarioId,
                        administradorId
                )
        );

        // Debe responder 403 Forbidden
        assertEquals(
                HttpStatus.FORBIDDEN,
                error.getStatusCode()
        );

        // No debe consultar ni eliminar usuarios en MySQL
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void impideQueAdministradorEditeSuPropiaCuenta() {

        // ID de la cuenta que se intenta modificar
        Long usuarioId = 5L;

        // ID del Administrador autenticado
        String administradorId = "5";

        // Simular los datos de actualizacion
        ActualizarUsuarioRequest request =
                mock(ActualizarUsuarioRequest.class);

        // Intentar modificar la propia cuenta
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.actualizarUsuario(
                        usuarioId,
                        administradorId,
                        request
                )
        );

        // Debe responder 403 Forbidden
        assertEquals(
                HttpStatus.FORBIDDEN,
                error.getStatusCode()
        );

        // No debe consultar ni actualizar usuarios en MySQL
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void listaUsuariosCorrectamente() {

        // Preparar un usuario de prueba
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombre("Maria");
        usuario.setApellido("Gonzalez");
        usuario.setCorreo("maria@gmail.com");
        usuario.setRol(new Rol("Cliente"));
        usuario.setTipo("Socio VIP");
        usuario.setActivo(true);

        // Simular los usuarios almacenados en MySQL
        when(usuarioRepository.findAll())
                .thenReturn(List.of(usuario));

        // Ejecutar el listado
        List<UsuarioResumenResponse> respuesta =
                usuarioService.listarUsuarios();

        // Comprobar la cantidad de usuarios
        assertEquals(1, respuesta.size());

        // Comprobar los datos devueltos
        UsuarioResumenResponse resultado = respuesta.get(0);

        assertEquals(5L, resultado.id());
        assertEquals("Maria", resultado.nombre());
        assertEquals("Gonzalez", resultado.apellido());
        assertEquals("maria@gmail.com", resultado.correo());
        assertEquals("Cliente", resultado.rol());
        assertEquals("Socio VIP", resultado.tipo());
        assertEquals(true, resultado.activo());

        // Comprobar que se consulto el repositorio
        verify(usuarioRepository).findAll();
    }

    // PRUEBA 10: Crear una cuenta interna correctamente
    @Test
    void creaUsuarioInternoCorrectamente() {

        CrearUsuarioInternoRequest request =
                new CrearUsuarioInternoRequest(
                        "Ana",
                        "Perez",
                        "ANA@JUGOSEO.COM",
                        "Password123",
                        "Locutor"
                );

        // Simular el rol y el cifrado de la contraseña
        when(rolRepository.findByNombre("Locutor"))
                .thenReturn(Optional.of(new Rol("Locutor")));

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashSimulado");

        // Simular el guardado en MySQL
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> {
                    Usuario usuario = invocacion.getArgument(0);
                    usuario.setId(10L);
                    return usuario;
                });

        RegistroUsuarioResponse respuesta =
                usuarioService.crearUsuarioInterno(request);

        // Comprobar la respuesta
        assertEquals(10L, respuesta.id());
        assertEquals("Ana", respuesta.nombre());
        assertEquals("ana@jugoseo.com", respuesta.correo());
        assertEquals("Locutor", respuesta.rol());

        // Comprobar los datos enviados al repositorio
        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();

        assertEquals("hashSimulado", guardado.getPasswordHash());
        assertEquals("ana@jugoseo.com", guardado.getCorreo());
        assertEquals("Locutor", guardado.getRol().getNombre());
    }

    // PRUEBA 11: Actualizar un usuario correctamente
    @Test
    void actualizaUsuarioCorrectamente() {

        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Ana");
        usuario.setApellido("Perez");
        usuario.setCorreo("ana@gmail.com");
        usuario.setRol(new Rol("Cliente"));
        usuario.setTipo("Socio VIP");
        usuario.setActivo(true);
        usuario.setVersionCuenta(2L);

        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        ActualizarUsuarioRequest request =
                new ActualizarUsuarioRequest(
                        "Ana Maria",
                        "Perez",
                        "Cliente",
                        "Radioescucha Oficial",
                        ""
                );

        UsuarioResumenResponse respuesta =
                usuarioService.actualizarUsuario(
                        7L,
                        "99",
                        request
                );

        // Comprobar los datos actualizados
        assertEquals("Ana Maria", respuesta.nombre());
        assertEquals("Radioescucha Oficial", respuesta.tipo());

        // Comprobar invalidacion de sesiones antiguas
        assertEquals(3L, usuario.getVersionCuenta());

        verify(usuarioRepository).save(usuario);
    }

    // PRUEBA 12: Eliminar un usuario correctamente
    @Test
    void eliminaUsuarioCorrectamente() {

        Usuario usuario = new Usuario();
        usuario.setId(7L);

        when(usuarioRepository.findById(7L))
                .thenReturn(Optional.of(usuario));

        // El Administrador 99 elimina al usuario 7
        usuarioService.eliminarUsuario(7L, "99");

        // Verificar que se solicito la eliminacion
        verify(usuarioRepository).delete(usuario);
    }
}
