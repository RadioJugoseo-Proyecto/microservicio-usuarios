package cl.radiojugoseo.usuariosroles.service;

import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioRequest;
import cl.radiojugoseo.usuariosroles.dto.RegistroUsuarioResponse;
import cl.radiojugoseo.usuariosroles.entity.Rol;
import cl.radiojugoseo.usuariosroles.entity.Usuario;
import cl.radiojugoseo.usuariosroles.repository.RolRepository;
import cl.radiojugoseo.usuariosroles.repository.UsuarioRepository;
import cl.radiojugoseo.usuariosroles.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import cl.radiojugoseo.usuariosroles.dto.LoginResponse;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegistroUsuarioResponse registrarCliente(
            RegistroUsuarioRequest request) {

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado");
        }

        String run = request.run();

        if (run != null) {
            run = run.trim();

            if (run.isEmpty()) {
                run = null;
            }
        }

        if (run != null && usuarioRepository.existsByRun(run)) {
            throw new IllegalArgumentException(
                    "El RUN ya está registrado");
        }

        Rol rolCliente = rolRepository.findByNombre("Cliente")
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el rol Cliente"));

        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setCorreo(correo);
        usuario.setPasswordHash(
                passwordEncoder.encode(request.password()));
        usuario.setRun(run);
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setDireccion(request.direccion());
        usuario.setRegion(request.region());
        usuario.setComuna(request.comuna());
        usuario.setTipo(request.tipo());
        usuario.setRol(rolCliente);

        Usuario guardado = usuarioRepository.save(usuario);

        return new RegistroUsuarioResponse(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getCorreo(),
                rolCliente.getNombre()
        );
    }

    @Transactional(readOnly = true)
    public Usuario verificarCredenciales(LoginRequest request) {

        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Buscar al usuario por su correo
        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales incorrectas"
                ));

        // Comprobar que la cuenta esté activa
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas"
            );
        }

        // Comparar la contraseña con el hash BCrypt
        if (!passwordEncoder.matches(
                request.password(),
                usuario.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas"
            );
        }

        return usuario;
    }


    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {

        // Verificar correo, contraseña y estado de la cuenta
        Usuario usuario = verificarCredenciales(request);

        // Obtener el rol del usuario
        String rol = usuario.getRol().getNombre();

        // Generar el token JWT
        String token = jwtService.generarToken(
                usuario.getId(),
                usuario.getCorreo(),
                rol
        );

        // Devolver la información del inicio de sesión
        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                rol
        );
    }

}
