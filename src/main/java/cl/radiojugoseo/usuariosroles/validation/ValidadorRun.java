package cl.radiojugoseo.usuariosroles.validation;
import java.util.Locale;

public class ValidadorRun {
    private ValidadorRun() {
    }

    public static String validar(String runIngresado) {
        if (runIngresado == null
                || runIngresado.trim().isEmpty()) {
            return "El RUN es obligatorio.";
        }
        String run = runIngresado.trim()
                .toUpperCase(Locale.ROOT);

        if (run.contains(".") || run.contains("-")) {
            return "El RUN no debe llevar puntos ni guion.";
        }
        if (run.length() < 7 || run.length() > 9) {
            return "El RUN debe tener entre 7 y 9 caracteres.";
        }
        if (!run.matches("^[0-9]+[0-9K]$")) {
            return "El RUN debe contener números y un dígito verificador válido.";
        }
        String cuerpo = run.substring(0, run.length() - 1);
        char digito = run.charAt(run.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int numero = Character.getNumericValue(
                    cuerpo.charAt(i));
            suma += numero * multiplicador;
            multiplicador = multiplicador == 7
                    ? 2
                    : multiplicador + 1;
        }

        int resultado = 11 - (suma % 11);
        char esperado;

        if (resultado == 11) {
            esperado = '0';
        } else if (resultado == 10) {
            esperado = 'K';
        } else {
            esperado = Character.forDigit(resultado, 10);
        }

        if (digito != esperado) {
            return "El RUN ingresado tiene un dígito verificador incorrecto.";
        }
        return "";
    }
}
