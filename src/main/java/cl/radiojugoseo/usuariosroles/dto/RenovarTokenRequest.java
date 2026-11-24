package cl.radiojugoseo.usuariosroles.dto;

import jakarta.validation.constraints.NotBlank;

public record RenovarTokenRequest(

    @NotBlank(message = "El token de renovacion es obligatorio")
    String tokenRenovacion

) {
}
