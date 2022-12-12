package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    private int generateId() {
        return ++uid;
    }
}
