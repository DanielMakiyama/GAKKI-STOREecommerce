package com.gakki.store.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// RNF0031 — senha com no mínimo 8 caracteres, contendo maiúscula, minúscula e caractere especial.

@Documented
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT})
@Retention(RUNTIME)
@ReportAsSingleViolation
@Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s]).{8,}$",
        message = "A senha deve ter no mínimo 8 caracteres, com letra maiúscula, minúscula e caractere especial"
)
public @interface SenhaForte {

    String message() default "A senha deve ter no mínimo 8 caracteres, com letra maiúscula, minúscula e caractere especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
