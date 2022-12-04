package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void emptyEmailInUser() {
        final User user = new User("", "login", "name", LocalDate.of(1990, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        assertEquals(2, violation.size());
    }

    @Test
    void notEmailFormatInUser() {
        final User user = new User("mail.ru", "login", "name", LocalDate.of(1990, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        assertEquals(2, violation.size());
    }

    @Test
    void emptyLoginInUser() {
        final User user = new User("ya@mail.ru", "", "name", LocalDate.of(1990, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        assertEquals(1, violation.size());
    }

    @Test
    void nonCorrectLoginInUser() {
        final User user = new User("ya@mail.ru", "asd tru", "name", LocalDate.of(1990, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        assertEquals(1, violation.size());
    }

    @Test
    void emptyNameInUser() {
        final User user = new User("ya@mail.ru", "login", "", LocalDate.of(1990, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        System.out.println(user);
        assertEquals(0, violation.size());
        UserController controller = new UserController();
        controller.create(user);
        assertEquals(user.getLogin(), controller.getUsers().iterator().next().getName(),
                "Замена name на login не состоялась");
    }

    @Test
    void birthdayInFutureInUser() {
        final User user = new User("ya@mail.ru", "Login", "name", LocalDate.of(2030, 12, 01));
        Set<ConstraintViolation<User>> violation = validator.validate(user);
        System.out.println(violation);
        assertEquals(2, violation.size());
    }


}
