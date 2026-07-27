package com.kodocode.api.lead;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoCodeValidator implements ConstraintValidator<NoCode, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !ContactTextPolicy.containsCode(value);
    }
}
