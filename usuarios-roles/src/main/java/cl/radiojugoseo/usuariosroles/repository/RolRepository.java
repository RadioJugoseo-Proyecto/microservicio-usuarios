package cl.radiojugoseo.usuariosroles.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.radiojugoseo.usuariosroles.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
