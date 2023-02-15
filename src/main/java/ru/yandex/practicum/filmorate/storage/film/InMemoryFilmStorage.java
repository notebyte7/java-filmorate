package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private int uid;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        int id = generateId();
        film.setId(id);
        films.put(id, film);
        log.debug("Film добавлен в базу, текущее количество фильмов: {}", films.size());
        return film;
    }

    @Override
    public Film update(Film film) {
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

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(int id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new FilmNotFoundException("Фильм с " + id + " не найден");
        }
    }

    @Override
    public void addLike(int id, int userId) {
        Film film = getFilmById(id);
        film.getWhoLikedUserIds().add(userId);
        update(film);
    }

    @Override
    public void removeLike(int id, int userId) {
        Film film = getFilmById(id);
        film.getWhoLikedUserIds().remove(userId);
        update(film);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return getFilms().stream().sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Genre> getGenres() {
        return null;
    }

    @Override
    public Collection<Genre> getFilmGenresById(int id) {
        return null;
    }

    @Override
    public Collection<MPA> getMpa() {
        return null;
    }

    @Override
    public MPA getMpaById(int id) {
        return null;
    }

    @Override
    public Genre getGenreById(int id) {
        return null;
    }

    private int compare(Film f1, Film f2) {
        return f2.getWhoLikedUserIds().size() - f1.getWhoLikedUserIds().size();
    }

    private int generateId() {
        return ++uid;
    }
}
