package cl.radiojugoseo.usuariosroles.dto;
public record RegistroUsuarioResponse(
    Long id,
    String nombre,
    String apellido,
    String correo,
    String rol
) {
}
