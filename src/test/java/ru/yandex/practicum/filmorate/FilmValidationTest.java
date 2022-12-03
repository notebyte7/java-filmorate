package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void emptyFilmNameTest() {
        final Film film = new Film("", "Description film 1",
                LocalDate.of(2022, 12, 01), 100);
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        System.out.println(violation);
        assertEquals(1, violation.size());
    }

    @Test
    void filmDescription200Test() {
        final Film film = new Film("Film1", "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tatio",
                LocalDate.of(2022, 12, 01), 100);
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        System.out.println(violation);
        assertEquals(0, violation.size());
    }

    @Test
    void filmDescription201Test() {
        final Film film = new Film("Film1", "ALorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tatio",
                LocalDate.of(2022, 12, 01), 100);
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        System.out.println(violation);
        assertEquals(1, violation.size());
    }

    @Test
    void nonCorrectReleaseDate() {
        FilmController controller = new FilmController();
        final Film film = new Film("Film1", "Description film 1",
                LocalDate.of(1895, 12, 27), 100);
        assertThrows(ValidationException.class, () -> {
            controller.create(film);
        });
    }

    @Test
    void correctReleaseDate() {
        FilmController controller = new FilmController();
        final Film film = new Film("Film1", "Description film 1",
                LocalDate.of(1895, 12, 28), 100);
        controller.create(film);
    }

    @Test
    void durationNonPositive() {
        final Film film = new Film("Film1", "Description",
                LocalDate.of(2022, 12, 01), 0);
        Set<ConstraintViolation<Film>> violation = validator.validate(film);
        System.out.println(violation);
        assertEquals(1, violation.size());
    }
}
