package cl.radiojugoseo.usuariosroles.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunConstraintValidator
        implements ConstraintValidator<RunValido, String> {

    @Override
    public boolean isValid(
            String run,
            ConstraintValidatorContext context) {

        // Los valores vacíos se revisan con @NotBlank.
        if (run == null || run.isBlank()) {
            return true;
        }

        String error = ValidadorRun.validar(run);

        if (error.isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(error)
                .addConstraintViolation();

        return false;
    }
}