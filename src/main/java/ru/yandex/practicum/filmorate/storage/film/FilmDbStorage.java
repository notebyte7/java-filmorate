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

    private Collection<Genre> updateGenres(Film film) {
        String sql = "DELETE FROM PUBLIC.FILM_GENRES WHERE FILM_ID = ?";
        jdbcTemplate.update(sql, film.getId());
        Set<Genre> genres = new LinkedHashSet<>();
        if (film.getGenres() != null) {
            genres = new LinkedHashSet<>(film.getGenres());
            for (Genre genre : genres) {
                String sqlQuery = "INSERT INTO PUBLIC.FILM_GENRES " +
                        "(FILM_ID, GENRE_ID) " +
                        "VALUES (?, ?) ";
                jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
            }
        } return genres;
    }

    @Override
    public Film update(Film film) {
        int id = film.getId();
        String sqlQuery = "UPDATE PUBLIC.FILMS " +
                "SET NAME= ?, DESCRIPTION= ?, RELEASE_DATE= ?, DURATION= ?, RATING= ? " +
                "WHERE ID= ?";
        int updateStatus = jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), id);
        if (updateStatus != 0) {
            film.setGenres(updateGenres(film));
            return film;
        } else {
            return null;
        }
    }

    @Override
    public Collection<Film> getFilms() {
        Map<Integer, Set<Genre>> allGenres = getFilmGenres();
        String sqlQuery = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID AS mpa_id, " +
                "m.NAME AS mpa_name " +
                "FROM FILMS AS f " +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs, allGenres));
    }

    private Film makeFilm(ResultSet rs, Map<Integer, Set<Genre>> allGenres) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        MPA mpa = new MPA(rs.getInt("mpa_id"), rs.getString("mpa_name"));
        Set<Genre> genres = new LinkedHashSet<>();
        if (allGenres.get(id) != null) {
            genres = allGenres.get(id);
        }
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

    private Map<Integer, Set<Genre>> getFilmGenres() {
        Map<Integer, Set<Genre>> allGenres = new LinkedHashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT fg.FILM_ID, fg.GENRE_ID, g.NAME " +
                "FROM FILM_GENRES fg " +
                "LEFT JOIN GENRES g ON fg.GENRE_ID = g.ID ");

            while (rs.next()) {
                Integer filmId = rs.getInt(1);
                allGenres.putIfAbsent(filmId, new LinkedHashSet<>());
                Genre genre = new Genre(rs.getInt(2), rs.getString(3));
                allGenres.get(filmId).add(genre);
            }
            return allGenres;
    }

    @Override
    public Film getFilmById(int id) {
        Set<Genre> genres = new LinkedHashSet<>();
        if (getFilmGenres().get(id) != null) {
            genres = getFilmGenres().get(id);
        }
                getFilmGenres().get(id);
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
                    genres,
                    new MPA(filmRows.getInt(6), filmRows.getString(7)),
                    getFilmLikes(id));
            return film;
        } else {
            return null;
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
        Map<Integer, Set<Genre>> allGenres = getFilmGenres();
        return jdbcTemplate.query("SELECT F.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID AS mpa_id, " +
                "m.NAME AS mpa_name " +
                "FROM FILMS f " +
                "LEFT JOIN FILM_LIKES fl ON f.id = fl.FILM_ID " +
                "LEFT JOIN mpa AS m ON m.ID = f.RATING " +
                "GROUP BY F.ID " +
                "ORDER BY COUNT (user_id) DESC " +
                "LIMIT ?", (rs, rowNum) -> makeFilm(rs, allGenres), count);
    }

    @Override
    public Collection<Genre> getGenres() {
        SqlRowSet GenreRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM GENRES ");
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
                "FROM GENRES " +
                "WHERE ID = ?", id);
        if (GenreRows.next()) {
            Genre genre = new Genre(GenreRows.getInt(1), GenreRows.getString(2));
            return genre;
        } else {
            return null;
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
            return null;
        }
    }
}
