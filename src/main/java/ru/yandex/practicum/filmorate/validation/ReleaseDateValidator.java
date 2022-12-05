package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateValidation, LocalDate> {
    private final LocalDate firstReleaseDate = LocalDate.of(1895,12,28);

    @Override
    public void initialize(ReleaseDateValidation releaseDate) {
    }

    @Override
    public boolean isValid(LocalDate releaseDate,
                           ConstraintValidatorContext cxt) {
        return (!releaseDate.isBefore(firstReleaseDate));
    }
}
