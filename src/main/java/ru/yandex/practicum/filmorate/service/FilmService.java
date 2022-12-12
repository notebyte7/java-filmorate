package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(int id, int userId) {
        Film film = getFilmById(id);
        if (film != null && userId > 0) {
            film.getLikes().add(userId);
            filmStorage.update(film);
        } else {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }
    }

    public void removeLike(int id, int userId) {
        Film film = getFilmById(id);
        if (film != null) {
            if (film.getLikes().contains(userId)) {
                film.getLikes().remove(userId);
                filmStorage.update(film);
            } else {
                throw new FilmException("Like на фильм " + id + " от пользователя " + userId + " не найден");
            }
        }
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream().sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
    }

    private int compare(Film f1, Film f2) {
        return f2.getLikes().size() - f1.getLikes().size();
    }
}
