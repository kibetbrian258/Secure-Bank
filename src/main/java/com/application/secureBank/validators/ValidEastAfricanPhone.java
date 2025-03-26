package com.application.secureBank.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for East African phone numbers
 */
@Documented
@Constraint(validatedBy = EastAfricanPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEastAfricanPhone {
    String message() default "Phone number must be a valid East African phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}