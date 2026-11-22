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
import java.util.List;
import cl.radiojugoseo.usuariosroles.dto.UsuarioResumenResponse;
import cl.radiojugoseo.usuariosroles.dto.CrearUsuarioInternoRequest;
import cl.radiojugoseo.usuariosroles.dto.ActualizarUsuarioRequest;

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

    // Obtener la lista de usuarios registrados
    @Transactional(readOnly = true)
    public List<UsuarioResumenResponse> listarUsuarios() {

        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> new UsuarioResumenResponse(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        usuario.getCorreo(),
                        usuario.getRol().getNombre(),
                        usuario.getTipo(),
                        usuario.getActivo(),
                        usuario.getFechaCreacion()
                ))
                .toList();
    }


    // Crear una cuenta interna de Locutor o Administrador
    @Transactional
    public RegistroUsuarioResponse crearUsuarioInterno(
            CrearUsuarioInternoRequest request) {

        // Normalizar el correo
        String correo = request.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Comprobar que el correo no esté registrado
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        // Buscar el rol solicitado en MySQL
        Rol rol = rolRepository.findByNombre(request.rol())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El rol seleccionado no existe"
                ));

        // Crear la entidad del nuevo usuario
        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setCorreo(correo);
        usuario.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        usuario.setRol(rol);

        // Guardar en MySQL
        Usuario guardado = usuarioRepository.save(usuario);

        // Devolver los datos del usuario creado
        return new RegistroUsuarioResponse(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getCorreo(),
                rol.getNombre()
        );
    }

    // Actualizar una cuenta desde el panel Administrador
    @Transactional
    public UsuarioResumenResponse actualizarUsuario(
            Long id,
            String idAdministrador,
            ActualizarUsuarioRequest request) {

        // Impedir que el Administrador edite su propia cuenta
        if (String.valueOf(id).equals(idAdministrador)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar tu propia cuenta desde este panel"
            );
        }

        // Buscar el usuario en MySQL
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        String rolActual = usuario.getRol().getNombre();
        String rolNuevo = request.rol();

        // No permitir convertir Clientes en cuentas internas
        // ni convertir cuentas internas en Clientes
        boolean esCliente = "Cliente".equals(rolActual);
        boolean seraCliente = "Cliente".equals(rolNuevo);

        if (esCliente != seraCliente) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cambio de tipo de cuenta no está permitido"
            );
        }

        // Actualizar nombre y apellido
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());

        // Validar tipo de oyente cuando corresponde
        if (seraCliente) {

            if (!"Socio VIP".equals(request.tipo())
                    && !"Radioescucha Oficial".equals(request.tipo())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El tipo de oyente no es válido"
                );
            }

            usuario.setTipo(request.tipo());

        } else {
            usuario.setTipo(null);
        }

        // Actualizar el rol cuando haya cambiado
        if (!rolActual.equals(rolNuevo)) {

            Rol nuevoRol = rolRepository.findByNombre(rolNuevo)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El rol seleccionado no existe"
                    ));

            usuario.setRol(nuevoRol);
        }

        // Cambiar la contraseña solo cuando se proporciona una nueva
        if (request.password() != null
                && !request.password().isEmpty()) {

            usuario.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
        }

        // Registrar una nueva versión de la cuenta
        usuario.setVersionCuenta(usuario.getVersionCuenta() + 1);

        // Guardar los cambios
        Usuario actualizado = usuarioRepository.save(usuario);

        // Devolver información sin exponer la contraseña
        return new UsuarioResumenResponse(
                actualizado.getId(),
                actualizado.getNombre(),
                actualizado.getApellido(),
                actualizado.getCorreo(),
                actualizado.getRol().getNombre(),
                actualizado.getTipo(),
                actualizado.getActivo(),
                actualizado.getFechaCreacion()
        );
    }

    // Eliminar una cuenta desde el panel Administrador
    @Transactional
    public void eliminarUsuario(Long id, String idAdministrador) {

        // Impedir que el Administrador elimine su propia cuenta
        if (String.valueOf(id).equals(idAdministrador)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar tu propia cuenta"
            );
        }

        // Buscar el usuario que se desea eliminar
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        // Eliminar el usuario de MySQL
        usuarioRepository.delete(usuario);
    }

}