package cl.radiojugoseo.usuariosroles.validation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({
    ElementType.FIELD,
    ElementType.PARAMETER,
    ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RunConstraintValidator.class)
public @interface RunValido {

    String message() default "El RUN no es válido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
