package cl.radiojugoseo.usuariosroles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String apellido,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "Cliente|Locutor|Administrador",
        message = "El rol seleccionado no es válido"
    )
    String rol,

    String tipo,

    @Pattern(
        regexp = "^$|^.{8,10}$",
        message = "La contraseña debe tener entre 8 y 10 caracteres"
    )
    String password

) {
}
