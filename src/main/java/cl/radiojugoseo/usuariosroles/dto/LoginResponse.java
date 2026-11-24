package cl.radiojugoseo.usuariosroles.dto;

public record LoginResponse(

    String token,
    String tipoToken,
    Long id,
    String nombre,
    String correo,
    String rol,
    String tokenRenovacion

) {

    // Mantener compatibilidad con el login actual
    public LoginResponse(
            String token,
            String tipoToken,
            Long id,
            String nombre,
            String correo,
            String rol) {

        this(
                token,
                tipoToken,
                id,
                nombre,
                correo,
                rol,
                null
        );
    }
}
