package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
            Film newFilm =  filmStorage.update(film);
        if (newFilm != null) {
            return newFilm;
        }
        throw new NotFoundException("Фильм с таким id не существует");
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        Film newFilm = filmStorage.getFilmById(id);
        if (newFilm != null) {
            return newFilm;
        } else {
            throw new NotFoundException("Фильм не существует");
        }
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
        Genre genre = filmStorage.getGenreById(id);
        if (genre != null) {
            return genre;
        } else {
            throw new NotFoundException("Такого жанра не существует");
        }

    }

    public Collection<MPA> getMpa() {
        return filmStorage.getMpa();
    }

    public MPA getMpaById(int id) {
        MPA mpa = filmStorage.getMpaById(id);
        if (mpa != null) {
            return mpa;
        } else {
            throw new NotFoundException("Такого MPA не существует");
        }
    }
}
