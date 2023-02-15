package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.Collection;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    Collection<Film> getFilms();

    Film getFilmById(int id);

    void addLike(int id, int userId);

    void removeLike(int id, int userId);

    Collection<Film> getPopularFilms(int count);

    Collection<Genre> getGenres();

    Collection<Genre> getFilmGenresById(int id);

    Collection<MPA> getMpa();

    MPA getMpaById(int id);

    Genre getGenreById(int id);

}
