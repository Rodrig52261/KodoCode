package com.kodocode.api.lead;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoCodeValidator.class)
public @interface NoCode {
    String message() default "Nao insira codigo, HTML ou scripts.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
