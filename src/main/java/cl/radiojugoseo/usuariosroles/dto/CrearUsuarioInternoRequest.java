package cl.radiojugoseo.usuariosroles.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearUsuarioInternoRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String apellido,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 100)
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 72,
        message = "La contraseña debe tener entre 8 y 72 caracteres"
    )
    String password,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "Locutor|Administrador",
        message = "El rol debe ser Locutor o Administrador"
    )
    String rol

) {
}
