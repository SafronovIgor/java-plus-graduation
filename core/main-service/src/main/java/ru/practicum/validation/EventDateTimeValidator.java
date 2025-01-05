package ru.practicum.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class EventDateTimeValidator implements ConstraintValidator<LaterThan, LocalDateTime> {
    private LocalDateTime earliestDateTime;

    @Override
    public void initialize(LaterThan constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        earliestDateTime = LocalDateTime.now().plusHours(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDateTime localDateTime, ConstraintValidatorContext constraintValidatorContext) {
        if (localDateTime == null) {
            return true;
        }
        return localDateTime.isAfter(earliestDateTime);
    }
}
