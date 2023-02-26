package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@RestController
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping(value = "/films")
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films - создание Film");
        return filmService.create(film);
    }

    @PutMapping(value = "/films")
    public Film update(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films - обновление Film");
        return filmService.update(film);
    }

    @GetMapping(value = "/films")
    public Collection<Film> getFilms() {
        log.debug("Получен запрос GET /films - получить все Film");
        return filmService.getFilms();
    }

    @GetMapping(value = "/films/{id}")
    public Film getFilmById(@PathVariable int id) {
        log.debug("Получен запрос GET //films/{id} - получить Film по id");
        return filmService.getFilmById(id);
    }

    @PutMapping(value = "/films/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос PUT /films/{id}/like/{userId} - добавить лайк фильму с id от пользователя с userId");
        filmService.addLike(id, userId);
    }

    @DeleteMapping(value = "/films/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /films/{id}/like/{userId} - убрать лайк фильму с id от пользователя с userId");
        filmService.removeLike(id, userId);
    }

    @GetMapping(value = "/films/popular")
    public Collection<Film> popularFilms(@RequestParam(required = false, defaultValue = "10") Integer count) {
        log.debug("Получен запрос GET /films/popular - список популярных фильмов с параметром count");
        return filmService.getPopularFilms(count);
    }

    @GetMapping(value = "/genres")
    public Collection<Genre> getGenres() {
        log.debug("Получен запрос GET /genres - получить все Genre");
        return filmService.getGenres();
    }

    @GetMapping(value = "/genres/{id}")
    public Genre getGenreById(@PathVariable int id) {
        log.debug("Получен запрос GET /genres/{id} - получить Genre по id");
        return filmService.getGenreById(id);
    }

    @GetMapping(value = "/mpa")
    public Collection<MPA> getMpa() {
        log.debug("Получен запрос GET /mpa - получить все MPA");
        return filmService.getMpa();
    }

    @GetMapping(value = "/mpa/{id}")
    public MPA getMpaById(@PathVariable int id) {
        log.debug("Получен запрос GET /mpa/{id} - получить MPA по id");
        return filmService.getMpaById(id);
    }

}
