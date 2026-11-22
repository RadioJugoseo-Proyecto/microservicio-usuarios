package cl.radiojugoseo.usuariosroles.dto;

import java.time.LocalDateTime;

public record UsuarioResumenResponse(
    Long id,
    String nombre,
    String apellido,
    String correo,
    String rol,
    String tipo,
    Boolean activo,
    LocalDateTime fechaCreacion
) {
}
