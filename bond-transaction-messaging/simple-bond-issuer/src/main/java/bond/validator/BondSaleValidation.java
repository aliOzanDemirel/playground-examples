package bond.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = BondSaleValidator.class)
@Target({METHOD})
@Retention(RUNTIME)
@Documented
public @interface BondSaleValidation {

    String message() default "Cannot sell this bond as requirements are not met!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
