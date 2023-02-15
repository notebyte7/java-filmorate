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
import ru.yandex.practicum.filmorate.exception.FilmException;
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

        updateGenres(film);
        log.debug("Film добавлен в базу, текущее количество фильмов");
        return film;
    }

    private void updateGenres(Film film) {
        String sql = "DELETE FROM PUBLIC.GENRES WHERE FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String sqlQuery = "MERGE INTO PUBLIC.GENRES " +
                        "(FILM_ID, GENRE_ID) KEY (FILM_ID, GENRE_ID) " +
                        "VALUES (?, ?) ";
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
        if (film.getGenres() != null) {
            Set<Genre> genreSet = new LinkedHashSet<>(film.getGenres());
            film.setGenres(genreSet);
        }
        if (updateStatus != 0) {
            updateGenres(film);
            return film;
        } else {
            throw new FilmNotFoundException("Фильм для обновления не найден");
        }
    }

    @Override
    public Collection<Film> getFilms() {
        String sqlQuery = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID AS mpa_id, " +
                "m.NAME AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs));
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");
        MPA mpa = new MPA(rs.getInt("mpa_id"), rs.getString("mpa_name"));
        Collection<Genre> genres = getFilmGenresById(id);
        return new Film(id, name, description, releaseDate, duration, genres, mpa, getFilmLikes(id));
    }

    private Set<Integer> getFilmLikes(int id) {
        Set<Integer> whoLikedUserIds = new HashSet<>();
        SqlRowSet likesRow = jdbcTemplate.queryForRowSet("SELECT USER_ID FROM FILM_LIKES fl " +
                "WHERE FILM_ID  = ?", id);
        while (likesRow.next()) {
            whoLikedUserIds.add(likesRow.getInt(1));
        }
        return whoLikedUserIds;
    }

    public Collection<Genre> getFilmGenresById(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM FILMS AS f " +
                "WHERE f.id = ?", id);

        if (filmRows.next()) {
            SqlRowSet filmGenreRows = jdbcTemplate.queryForRowSet(
                    "SELECT ID, NAME FROM GENRE g " +
                            "WHERE ID IN (SELECT GENRE_ID " +
                            "FROM GENRES g " +
                            "WHERE FILM_ID = ?)", id);
            Collection<Genre> genres = new HashSet<>();
            while (filmGenreRows.next()) {
                Genre genre = new Genre(filmGenreRows.getInt(1), filmGenreRows.getString(2));
                genres.add(genre);
            }
            return genres;
        } else {
            throw new FilmNotFoundException("Фильма с id " + id + " не сущесутвует");
        }
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
                    getFilmGenresById(id),
                    new MPA(filmRows.getInt(6), filmRows.getString(7)),
                    getFilmLikes(id));
            return film;
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new FilmNotFoundException("Фильм с идентификатором не найден.");
        }
    }

    @Override
    public void addLike(int id, int userId) {
        SqlRowSet likeRow = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_LIKES WHERE film_id = ? AND user_id = ?",
                id, userId);
        if (likeRow.next()) {
            log.info("Лайк уже добавлен");
        } else {
            jdbcTemplate.update("INSERT INTO FILM_LIKES (film_id, user_id) " +
                    "VALUES (?, ?)", id, userId);
        }
    }

    @Override
    public void removeLike(int id, int userId) {
        SqlRowSet likeRow = jdbcTemplate.queryForRowSet("SELECT * FROM FILM_LIKES WHERE film_id = ? AND user_id = ?",
                id, userId);
        if (likeRow.next()) {
            jdbcTemplate.update("DELETE FROM FILM_LIKES " +
                    "WHERE film_id = ? AND user_id = ? ", id, userId);
        } else {
            log.info("Лайка нет");
        }
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return jdbcTemplate.query("SELECT F.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID AS mpa_id, " +
                "m.NAME AS mpa_name " +
                "FROM FILMS f " +
                "LEFT JOIN FILM_LIKES fl ON f.id = fl.FILM_ID " +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING " +
                "GROUP BY F.ID " +
                "ORDER BY COUNT (user_id) DESC " +
                "LIMIT ?", (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public Collection<Genre> getGenres() {
        SqlRowSet GenreRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM GENRE ");
        Collection<Genre> genres = new ArrayList<>();
        while (GenreRows.next()) {
            Genre genre = new Genre(GenreRows.getInt(1), GenreRows.getString(2));
            genres.add(genre);
        }
        return genres;
    }

    @Override
    public Genre getGenreById(int id) {
        SqlRowSet GenreRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM GENRE " +
                "WHERE ID = ?", id);
        if (GenreRows.next()) {
            Genre genre = new Genre(GenreRows.getInt(1), GenreRows.getString(2));
            return genre;
        } else {
            throw new FilmException("Нет такого жанра");
        }
    }

    @Override
    public Collection<MPA> getMpa() {
        SqlRowSet MpaRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM MPA ");
        Collection<MPA> mpas = new ArrayList<>();
        while (MpaRows.next()) {
            MPA mpa = new MPA(MpaRows.getInt(1), MpaRows.getString(2));
            mpas.add(mpa);
        }
        return mpas;
    }

    @Override
    public MPA getMpaById(int id) {
        SqlRowSet MpaRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM MPA m " +
                "WHERE ID = ?", id);
        if (MpaRows.next()) {
            MPA mpa = new MPA(MpaRows.getInt(1), MpaRows.getString(2));
            return mpa;
        } else {
            throw new FilmException("Нет MPA для фильма id = " + id);
        }
    }


}
