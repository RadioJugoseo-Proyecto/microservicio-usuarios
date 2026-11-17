package cl.radiojugoseo.usuariosroles.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.radiojugoseo.usuariosroles.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByRun(String run);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByRun(String run);
}
