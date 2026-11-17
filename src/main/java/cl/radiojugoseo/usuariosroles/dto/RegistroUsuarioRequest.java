package cl.radiojugoseo.usuariosroles.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import cl.radiojugoseo.usuariosroles.validation.RunValido;


public record RegistroUsuarioRequest(
//record permite representar datos sin escribir manualmente los getters y constructores

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
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    String password,

    @NotBlank(message = "El RUN es obligatorio")
    @RunValido 
    String run,

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    LocalDate fechaNacimiento,

    String direccion,

    @NotBlank(message = "Selecciona tu región")
    String region,

    @NotBlank(message = "Selecciona una comuna")
    String comuna,
    String tipo

) {
}
