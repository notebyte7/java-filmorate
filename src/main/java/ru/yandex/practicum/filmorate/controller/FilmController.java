package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class FilmController {

    private int uid;
    private final LocalDate firstReleaseDate = LocalDate.of(1895,12,28);
    private Map<Integer, Film> films = new HashMap<>();

    @PostMapping(value = "/films")
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films - создание Film");
        int id = generateId();
        film.setId(id);
        if (film.getReleaseDate().isBefore(firstReleaseDate)) {
            log.debug("Ошибка валидации releaseDate");
            throw new ValidationException();
        }
        films.put(id, film);
        log.debug("Film добавлен в базу, текущее количество фильмов: {}", films.size());
        return film;
    }

    @PutMapping(value = "/films")
    public Film update(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films - обновление Film");
        int id = film.getId();
        if (films.containsKey(id)) {
            films.put(id, film);
            log.debug("Film успешно обновлен, текущее количество фильмов: {}", films.size());
        } else {
            log.debug("Фильм для обновления не найден");
            throw new FilmNotFoundException("Фильм для обновления не найден");
        }
        return film;
    }

    @GetMapping(value = "/films")
    public List<Film> getFilms() {
        log.debug("Получен запрос GET /films - получить все Film");
        return new ArrayList<>(films.values());
    }

    private int generateId() {
        return ++uid;
    }
}
