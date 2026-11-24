package cl.radiojugoseo.usuariosroles.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidadorRunTest {

    @Test
    void aceptaRunValido() {
        String resultado = ValidadorRun.validar("123456785");

        assertEquals("", resultado);
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        String resultado = ValidadorRun.validar("123456784");

        assertEquals(
                "El RUN ingresado tiene un dígito verificador incorrecto.",
                resultado
        );
    }

    @Test
    void rechazaRunConPuntosYGuion() {
        String resultado = ValidadorRun.validar("12.345.678-5");

        assertEquals(
                "El RUN no debe llevar puntos ni guion.",
                resultado
        );
    }

    @Test
    void rechazaRunVacio() {
        String resultado = ValidadorRun.validar(null);

        assertEquals(
                "El RUN es obligatorio.",
                resultado
        );
    }
}