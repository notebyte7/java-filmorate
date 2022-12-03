package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    @NotBlank(message = "Поле не должно быть пустым")
    private final String name;
    @Size(max = 200, message = "Длина не более 200 символов")
    private final String description;
    @NotNull(message = "Дата релиза не должна быть пустой")
    private final LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private final int duration;
}
