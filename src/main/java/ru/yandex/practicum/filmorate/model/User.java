package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotBlank
    @Email(message = "Должен быть email")
    private final String email;
    @Pattern(regexp = "\\S*", message = "Неверный формат")
    @NotNull
    private final String login;
    private String name;
    @PastOrPresent(message = "дата рождения не может быть в будущем")
    private final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}

