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

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
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
}
