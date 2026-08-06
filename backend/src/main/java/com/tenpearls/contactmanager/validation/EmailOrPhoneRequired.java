package com.tenpearls.contactmanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure either email or phone is provided.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailOrPhoneValidator.class)
public @interface EmailOrPhoneRequired {
    String message() default "Either email or phone is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
