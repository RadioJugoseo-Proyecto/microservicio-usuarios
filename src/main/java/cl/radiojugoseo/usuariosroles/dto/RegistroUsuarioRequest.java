package cl.radiojugoseo.usuariosroles.dto;
import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import cl.radiojugoseo.usuariosroles.validation.RunValido;
import java.util.Set;
import jakarta.validation.constraints.AssertTrue;

public record RegistroUsuarioRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String apellido,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    @Size(max = 100)
    @Pattern(
        regexp = "(?i)^[^\\s@]+@(gmail\\.com|duocuc\\.cl)$",
        message = "Las cuentas Cliente deben utilizar un correo @gmail.com o @duocuc.cl"
    )
    String correo,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 10,
        message = "La contraseña debe tener entre 8 y 10 caracteres"
    )
    String password,

    @NotBlank(message = "El RUN es obligatorio")
    @RunValido
    String run,

    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    LocalDate fechaNacimiento,

    @Size(
        max = 300,
        message = "La dirección no puede superar 300 caracteres"
    )
    String direccion,

    @NotBlank(message = "Selecciona tu región")
    String region,

    @NotBlank(message = "Selecciona una comuna")
    String comuna,

    @NotBlank(message = "Selecciona un tipo de registro")
    @Pattern(
        regexp = "Socio VIP|Radioescucha Oficial",
        message = "El tipo debe ser Socio VIP o Radioescucha Oficial"
    )
    String tipo

) {
    @AssertTrue(
        message = "La comuna no corresponde a la región seleccionada"
    )
    public boolean isComunaValidaParaRegion() {

        // Los campos obligatorios se validan con @NotBlank
        if (region == null || region.isBlank()
                || comuna == null || comuna.isBlank()) {
            return true;
        }

        // Opciones existentes en regionesComunas.js
        return switch (region) {

            case "Región Metropolitana" ->
                Set.of(
                    "Santiago", "Providencia", "Ñuñoa",
                    "Maipú", "Puente Alto"
                ).contains(comuna);

            case "Valparaíso" ->
                Set.of(
                    "Valparaíso", "Viña del Mar", "Quilpué",
                    "San Antonio", "Los Andes"
                ).contains(comuna);

            case "O'Higgins" ->
                Set.of(
                    "Rancagua", "San Fernando",
                    "Rengo", "Machalí"
                ).contains(comuna);

            case "Maule" ->
                Set.of(
                    "Talca", "Curicó",
                    "Linares", "Constitución"
                ).contains(comuna);

            case "Biobío" ->
                Set.of(
                    "Concepción", "Talcahuano",
                    "Los Ángeles", "Chillán"
                ).contains(comuna);

            case "Araucanía" ->
                Set.of(
                    "Temuco", "Villarrica",
                    "Angol", "Pucón"
                ).contains(comuna);

            case "Los Lagos" ->
                Set.of(
                    "Puerto Montt", "Osorno",
                    "Castro", "Puerto Varas"
                ).contains(comuna);

            case "Antofagasta" ->
                Set.of(
                    "Antofagasta", "Calama", "Tocopilla"
                ).contains(comuna);

            case "Coquimbo" ->
                Set.of(
                    "La Serena", "Coquimbo", "Ovalle"
                ).contains(comuna);

            // Rechazar regiones no incluidas en el formulario
            default -> false;
        };
    }
}
