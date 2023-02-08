package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private int uid;
    private final Map<Integer, Film> films = new HashMap<>();

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO PUBLIC.FILMS " +
                        "(NAME, DESCRIPTION, RELEASE_DATE, DURATION, RATING) " +
                        "VALUES(?, ?, ?, ?, ?);", new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getMpa().getId());
                return ps;
            }
        }, keyHolder);
        film.setId(keyHolder.getKey().intValue());

        setGenres(film);
        log.debug("Film добавлен в базу, текущее количество фильмов: {}", films.size());
        return film;
    }

    private void setGenres(Film film) {
        String sql = "DELETE FROM PUBLIC.GENRES WHERE FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() != null) {
            for (Genre genre:film.getGenres()){
                String sqlQuery = "INSERT INTO PUBLIC.GENRES " +
                        "(FILM_ID, GENRE_ID)" +
                        "VALUES (?, ?)";
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        }
    }

    @Override
    public Film update(Film film) {
        int id = film.getId();
        String sqlQuery = "UPDATE PUBLIC.FILMS " +
                "SET NAME= ?, DESCRIPTION= ?, RELEASE_DATE= ?, DURATION= ?, RATING= ? " +
                "WHERE ID= ?";
        int updateStatus = jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getId());
        if (updateStatus != 0) {
            setGenres(film);
            return film;
        } else {
            throw new FilmNotFoundException("Фильм для обновления не найден");
        }
    }
    @Override
    public Collection<Film> getFilms() {
        String sqlQuery = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID AS mpa_id, " +
                "m.NAME AS mpa_name\n" +
                "FROM FILMS AS f\n" +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs));
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");
        MPA mpa = new MPA(rs.getInt("mpa_id"), rs.getString("mpa_name"));
        List<Genre> genres = getGenresById(id);
        return new Film(id, name, description, releaseDate, duration, genres, mpa);
    }

    private List<Genre> getGenresById(Integer id) {
        SqlRowSet filmGenreRows = jdbcTemplate.queryForRowSet(
                "SELECT ID, NAME FROM GENRE g " +
                "WHERE ID IN (SELECT GENRE_ID " +
                "FROM GENRES g " +
                "WHERE FILM_ID = ?)", id);
        List<Genre> genres = new ArrayList<>();
        while (filmGenreRows.next()) {
            Genre genre = new Genre(filmGenreRows.getInt(1), filmGenreRows.getString(2));
            genres.add(genre);
        }
        return genres;
    }

    @Override
    public Film getFilmById(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, " +
                "f.DURATION, m.ID, m.NAME " +
                "FROM FILMS AS f " +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING WHERE f.id = ?", id);

        if (filmRows.next()) {
            Film film = new Film(filmRows.getInt("id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    getGenresById(id),
                    new MPA(filmRows.getInt(6), filmRows.getString(7)));
            return film;
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
             throw new FilmNotFoundException("Фильм с идентификатором не найден.");
        }
    }
}
