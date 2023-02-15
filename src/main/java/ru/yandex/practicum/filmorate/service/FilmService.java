package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
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
            filmStorage.addLike(id, userId);
        } else {
            throw new FilmException("Неправильные данные для ввода");
        }
    }

    public void removeLike(int id, int userId) {
        Film film = getFilmById(id);
        if (film != null && userId > 0) {
                filmStorage.removeLike(id, userId);
        } else {
            throw new FilmException("Неправильные данные для ввода");
        }
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Collection<Genre> getGenres() {
        return filmStorage.getGenres();
    }

    public Genre getGenreById(int id) {
        return filmStorage.getGenreById(id);
    }

    public Collection<MPA> getMpa() {
        return filmStorage.getMpa();
    }

    public MPA getMpaById(int id) {
        return filmStorage.getMpaById(id);
    }
}
