package cl.radiojugoseo.usuariosroles.dto;

public record LoginResponse(

    String token,
    String tipoToken,
    Long id,
    String nombre,
    String correo,
    String rol

) {
}
