
package cl.radiojugoseo.usuariosroles.dto;

import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionCorreoDtoTest {

    // Comprobar si el campo correo presenta errores
    private boolean correoInvalido(
            Validator validador,
            Object request) {

        return validador.validate(request)
                .stream()
                .anyMatch(error ->
                        "correo".equals(
                                error.getPropertyPath().toString()
                        )
                );
    }

    // Crear una solicitud de registro de Cliente
    private RegistroUsuarioRequest cliente(String correo) {

        return new RegistroUsuarioRequest(
                "Ana",
                "Perez",
                correo,
                "Clave1234",
                "123456785",
                null,
                null,
                "Valparaíso",
                "Valparaíso",
                "Socio VIP"
        );
    }

    // Crear una solicitud de cuenta interna
    private CrearUsuarioInternoRequest interno(String correo) {

        return new CrearUsuarioInternoRequest(
                "Ana",
                "Perez",
                correo,
                "Clave1234",
                "Locutor"
        );
    }

    @Test
    void validaDominiosPermitidosParaClientes() {

        try (ValidatorFactory fabrica =
                Validation.buildDefaultValidatorFactory()) {

            Validator validador = fabrica.getValidator();

            // Dominios permitidos
            assertFalse(correoInvalido(
                    validador, cliente("ana@gmail.com")));

            assertFalse(correoInvalido(
                    validador, cliente("ana@duocuc.cl")));

            // Dominios prohibidos
            assertTrue(correoInvalido(
                    validador, cliente("ana@example.com")));

            assertTrue(correoInvalido(
                    validador, cliente("ana@jugoseo.com")));
        }
    }

    @Test
    void validaDominiosPermitidosParaPersonalInterno() {

        try (ValidatorFactory fabrica =
                Validation.buildDefaultValidatorFactory()) {

            Validator validador = fabrica.getValidator();

            // Dominio corporativo permitido
            assertFalse(correoInvalido(
                    validador, interno("ana@jugoseo.com")));

            // Dominios prohibidos
            assertTrue(correoInvalido(
                    validador, interno("ana@gmail.com")));

            assertTrue(correoInvalido(
                    validador, interno("ana@duocuc.cl")));

            assertTrue(correoInvalido(
                    validador, interno("ana@example.com")));
        }
    }

    @Test
    void validaQueComunaCorrespondaARegion() {

        try (ValidatorFactory fabrica =
                Validation.buildDefaultValidatorFactory()) {

            Validator validador = fabrica.getValidator();

            RegistroUsuarioRequest valido =
                    new RegistroUsuarioRequest(
                            "Ana",
                            "Perez",
                            "ana@gmail.com",
                            "Clave1234",
                            "123456785",
                            null,
                            null,
                            "Valparaíso",
                            "Viña del Mar",
                            "Socio VIP"
                    );

            RegistroUsuarioRequest invalido =
                    new RegistroUsuarioRequest(
                            "Ana",
                            "Perez",
                            "ana@gmail.com",
                            "Clave1234",
                            "123456785",
                            null,
                            null,
                            "Valparaíso",
                            "Concepción",
                            "Socio VIP"
                    );

            // Una combinación válida no debe generar errores.
            assertTrue(validador.validate(valido).isEmpty());

            // Una combinación incorrecta debe ser rechazada.
            assertTrue(
                    validador.validate(invalido)
                            .stream()
                            .anyMatch(error ->
                                    "comunaValidaParaRegion".equals(
                                            error.getPropertyPath().toString()
                                    )
                            )
            );
        }
    }
}
