package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValidation;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotBlank(message = "Поле не должно быть пустым")
    private final String name;
    @Size(max = 200, message = "Длина не более 200 символов")
    private final String description;
    @NotNull(message = "Дата релиза не должна быть пустой")
    @ReleaseDateValidation(message = "Дата релиза не может быть после заявленной даты")
    private final LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private final int duration;
    private Set<Integer> likes = new HashSet<>();
}
