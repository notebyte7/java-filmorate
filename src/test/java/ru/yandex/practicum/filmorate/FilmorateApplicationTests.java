package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmoRateApplicationTests {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    Set<Integer> friendIds= new HashSet<>();
    private final User user0 = new User(1, "email@mail.ru", "login1", "name1",
            LocalDate.of(2023, 02, 10), friendIds);
    private final User user1upd = new User(1, "emailupd@mail.ru", "login1upd", "name1upd",
            LocalDate.of(2023, 02, 11), friendIds);
    private final User user2 = new User(2, "email2@mail.ru", "login2", "name2",
            LocalDate.of(2023, 01, 10), friendIds);
    private final User user3 = new User(3, "email3@mail.ru", "login3", "name3",
            LocalDate.of(2022, 01, 01), friendIds);

    private final LinkedHashSet<Genre> genres = new LinkedHashSet<>();
    private final MPA mpa = new MPA(1, "G");
    private Set<Integer> whoLikedUserIds = new HashSet();
    private final Film film0 = new Film(1, "name1", "description1",
            LocalDate.of(2022, 12, 01), 100, genres, mpa, whoLikedUserIds);
    private final Film film2 = new Film(2, "name2", "description2",
            LocalDate.of(2021, 12, 01), 102, genres, mpa, whoLikedUserIds);
    private final Film film3 = new Film(3, "name3", "description3",
            LocalDate.of(2008, 11, 03), 103, genres, mpa, whoLikedUserIds);
    private final Film film0upd = new Film(1, "name1upd", "description1upd",
            LocalDate.of(2022, 11, 01), 101, genres, mpa, new HashSet<>());

    @Test
    public void testCreateAndUpdateUser() {
        User user = userStorage.create(user0);
        User getUser = userStorage.getUserById(user.getId());
        assertEquals(user, getUser, "пользователи не совпадают");
        User userUpd = userStorage.update(user1upd);
        User getUserUpd = userStorage.getUserById(user.getId());
        assertEquals(userUpd, getUserUpd, "пользователи не совпадают");
    }

    @Test
    public void testGetUsers() {
        User user = userStorage.create(user0);
        ArrayList<User> testUserList = new ArrayList<>();
        testUserList.add(user);
        Collection<User> userList = userStorage.getUsers();
        assertEquals(testUserList, userList, "пользователи в списках не совпадают");
    }

    @Test
    public void testGetUserById() {
        User user1 = userStorage.create(user0);
        User newUser = userStorage.getUserById(1);
        assertEquals(user1, newUser, "пользователи не совпадают");
    }

    @Test
    public void addFriendAndRemoveFriend() {
        User newUser = userStorage.create(user0);
        User newUser2 = userStorage.create(user2);
        userStorage.addFriend(1, 2);
        Set<Integer> testFriendIds = new HashSet<>();
        testFriendIds.add(2);

        User getUser = userStorage.getUserById(1);
        assertEquals(getUser.getFriendIds(), testFriendIds, "списки не совпадают");

        userStorage.removeFriend(1, 2);
        getUser = userStorage.getUserById(1);
        testFriendIds.remove(2);
        assertEquals(getUser.getFriendIds(), testFriendIds, "списки не совпадают");
    }
    @Test
    public void getCommonFriends() {
        User newUser = userStorage.create(user0);
        User newUser2 = userStorage.create(user2);
        User newUser3 = userStorage.create(user3);
        userStorage.addFriend(1, 2);
        userStorage.addFriend(3, 2);
        Collection<User> commonFriend = userStorage.commonFriends(1, 3);
        List<User> userList = userStorage.getFriends(1);

        assertEquals(userList, commonFriend, "списки не совпадают");
    }

    @Test
    public void testCreateAndUpdateFilm() {
        Film film = filmStorage.create(film0);
        Film getFilm = filmStorage.getFilmById(film.getId());
        assertEquals(film, getFilm, "фильмы не совпадают");
        Film filmUpd = filmStorage.update(film0upd);
        Film getFilmUpd = filmStorage.getFilmById(film.getId());
        assertEquals(filmUpd, getFilmUpd, "фильмы не совпадают");
    }

    @Test
    public void testGetFilms() {
        Film film = filmStorage.create(film0);
        ArrayList<Film> testFilmList = new ArrayList<>();
        testFilmList.add(film0);
        Collection<Film> filmList = filmStorage.getFilms();
        assertEquals(testFilmList, filmList, "пользователи в списках не совпадают");
    }

    @Test
    public void testGetFilmById() {
        Film film = filmStorage.create(film0);
        Film getFilm = filmStorage.getFilmById(film.getId());
        assertEquals(film, getFilm, "фильмы не совпадают");
    }

    @Test
    public void addLikeAndRemoveLike() {
        Film newFilm = filmStorage.create(film0);
        User newUser = userStorage.create(user0);
        filmStorage.addLike(1, 1);
        Set<Integer> testLikes = new HashSet<>();
        testLikes.add(1);

        Film getFilm = filmStorage.getFilmById(1);
        assertEquals(getFilm.getWhoLikedUserIds(), testLikes, "списки не совпадают");

        filmStorage.removeLike(1, 1);
        getFilm = filmStorage.getFilmById(1);
        testLikes.remove(1);
        assertEquals(getFilm.getWhoLikedUserIds(), testLikes, "списки не совпадают");
    }

    @Test
    public void getPopularFilms() {

        Film newFilm = filmStorage.create(film0);
        Film newFilm2 = filmStorage.create(film2);
        Film newFilm3 = filmStorage.create(film3);
        User newUser = userStorage.create(user0);
        User newUser2 = userStorage.create(user2);
        User newUser3 = userStorage.create(user3);
        filmStorage.addLike(2, 1);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 3);
        filmStorage.addLike(1, 1);
        filmStorage.addLike(3, 1);
        filmStorage.addLike(3, 3);

        newFilm = filmStorage.getFilmById(1);
        newFilm2 = filmStorage.getFilmById(2);
        newFilm3 = filmStorage.getFilmById(3);

        Collection<Film> popularFilms = filmStorage.getPopularFilms(3);
        List<Film> testPopularFilms = new LinkedList<>();
        testPopularFilms.add(newFilm2);
        testPopularFilms.add(newFilm3);
        testPopularFilms.add(newFilm);

        assertEquals(popularFilms, testPopularFilms, "списки не совпадают");
    }

    @Test
    public void getGenresAndGenreById() {
        Collection<Genre> genres = filmStorage.getGenres();
        Collection<Genre> testGenres = new ArrayList<>();
        testGenres.add(new Genre(1, "Комедия"));
        testGenres.add(new Genre(2, "Драма"));
        testGenres.add(new Genre(3, "Мультфильм"));
        testGenres.add(new Genre(4, "Триллер"));
        testGenres.add(new Genre(5, "Документальный"));
        testGenres.add(new Genre(6, "Боевик"));
        assertEquals(testGenres, genres, "списки не совпадают");

        Genre genre = filmStorage.getGenreById(3);
        Genre genre3 = new Genre(3, "Мультфильм");
        assertEquals(genre, genre3, "списки не совпадают");
    }

    @Test
    public void getMPAAndMPAById() {
        Collection<MPA> mpas = filmStorage.getMpa();
        Collection<MPA> testMpa = new ArrayList<>();
        testMpa.add(new MPA(1, "G"));
        testMpa.add(new MPA(2, "PG"));
        testMpa.add(new MPA(3, "PG-13"));
        testMpa.add(new MPA(4, "R"));
        testMpa.add(new MPA(5, "NC-17"));
        assertEquals(mpas, testMpa, "списки не совпадают");

        MPA mpa = filmStorage.getMpaById(4);
        MPA mpa4 = new MPA(4, "R");
        assertEquals(mpa, mpa4, "списки не совпадают");
    }
}
