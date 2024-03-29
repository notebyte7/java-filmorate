package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO PUBLIC.USERS " +
                        "(EMAIL, LOGIN, NAME, BIRTHDAY) " +
                        "VALUES(?, ?, ?, ?);", new String[]{"id"});
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }
        }, keyHolder);
        user.setId(keyHolder.getKey().intValue());

        log.debug("Пользователь добавлен");
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();

        String sqlQuery = "UPDATE PUBLIC.USERS " +
                "SET EMAIL= ?, LOGIN= ?, NAME= ?, BIRTHDAY= ? " +
                "WHERE ID= ?";
        int updateStatus = jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), id);
        if (updateStatus != 0) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public Collection<User> getUsers() {
        Map<Integer, Set<Integer>> allFriends = getFriendIds();
        return jdbcTemplate.query("SELECT * " +
                "FROM USERS AS u ", (rs, rowNum) -> makeUser(rs, allFriends));
    }

    private User makeUser(ResultSet rs, Map<Integer, Set<Integer>> allFriends) throws SQLException {
        Integer id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        LocalDate birthday = rs.getDate("birthday").toLocalDate();
        Set<Integer> friendIds = new LinkedHashSet<>();
        if (allFriends.get(id) != null) {
            friendIds = allFriends.get(id);
        }
        return new User(id, email, login, name, birthday, friendIds);
    }

    private Map<Integer, Set<Integer>> getFriendIds() {
        Map<Integer, Set<Integer>> allFriends = new LinkedHashMap<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM USER_FRIENDS uf ");
        while (rs.next()) {
            Integer id = rs.getInt(1);
            allFriends.putIfAbsent(id, new LinkedHashSet<>());
            Integer friend = rs.getInt(2);
            allFriends.get(id).add(friend);
        }
        return allFriends;
    }

    private Set<Integer> getFriendIdsById(int id) {
        Set<Integer> friendIds = new LinkedHashSet<>();
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT FRIEND_ID " +
                "FROM USER_FRIENDS uf " +
                "WHERE USER_ID = ?", id);
        while (rs.next()) {
            friendIds.add(rs.getInt(1));
        }
        return friendIds;
    }

    @Override
    public User getUserById(int id) {
        Set<Integer> friendIds = new LinkedHashSet<>();
        if (getFriendIds().get(id) != null) {
            friendIds = getFriendIdsById(id);
        }
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM USERS AS u " +
                "WHERE ID = ?", id);

        if (userRows.next()) {
            User user = new User(userRows.getInt("id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate(),
                    friendIds);
            return user;
        } else {
            return null;
        }
    }

    private Friendship getFriendship(int userId, int friendId) {
        SqlRowSet friendshipRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM USER_FRIENDS " +
                "WHERE USER_ID = ? AND FRIEND_ID = ?", userId, friendId);

        if (friendshipRows.next()) {
            Friendship userFriendship = new Friendship(friendshipRows.getInt("user_id"),
                    friendshipRows.getInt("friend_id"),
                    friendshipRows.getBoolean("confirmation"));
            return userFriendship;
        } else {
            return null;
        }
    }

    public void addFriend(int userId, int friendId) {
        Friendship userFriendship = getFriendship(userId, friendId);

        if (userFriendship != null) {
            log.info("Уже в списке друзей");
        } else {
            Friendship friendFriendship = getFriendship(friendId, userId);
            if (friendFriendship != null) {
                jdbcTemplate.update("INSERT INTO USER_FRIENDS (USER_ID, FRIEND_ID, CONFIRMATION)" +
                        "VALUES (?, ?, ?)", userId, friendId, true);
                log.info("Добавлен в список друзей");

                jdbcTemplate.update("UPDATE PUBLIC.USER_FRIENDS " +
                        "SET CONFIRMATION = ?" +
                        "WHERE USER_ID= ? AND FRIEND_ID= ?", true, friendId, userId);
                log.info("Дружба подтверждена");
            } else {
                jdbcTemplate.update("INSERT INTO PUBLIC.USER_FRIENDS (USER_ID, FRIEND_ID, CONFIRMATION)" +
                        "VALUES (?, ?, ?)", userId, friendId, false);
                log.info("Заявка в друзья отправлена");
            }
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        Friendship userFriendship = getFriendship(userId, friendId);

        if (userFriendship != null) {
            if (userFriendship.isConfirmation()) {
                jdbcTemplate.update("UPDATE PUBLIC.USER_FRIENDS " +
                        "SET CONFIRMATION = ?" +
                        "WHERE USER_ID= ? AND FRIEND_ID= ?", false, friendId, userId);
                log.info("Отмена подтверждения друзья");

            }
            jdbcTemplate.update("DELETE FROM PUBLIC.USER_FRIENDS " +
                    "WHERE USER_ID= ? AND FRIEND_ID= ?", userId, friendId);
            log.info("Удаление из списка друзей");
        } else {
            log.info("Заявок в друзья нет");
        }
    }

    @Override
    public List<User> getFriends(int id) {
        List<User> userFriends = new LinkedList<>();
        Set<Integer> friendIds = getFriendIdsById(id);
        if (friendIds != null) {
            for (Integer friendId: friendIds) {
                User user= getUserById(friendId);
                userFriends.add(user);
            }
            return userFriends;
        } else {
            return new LinkedList<>();
        }

    }

    @Override
    public Collection<User> commonFriends(int id, int otherId) {
        Map<Integer, Set<Integer>> allFriends = getFriendIds();
        return jdbcTemplate.query("SELECT * FROM USERS u " +
                "WHERE ID IN (SELECT FRIEND_ID as ID " +
                "FROM USER_FRIENDS uf " +
                "WHERE USER_ID  = ? " +
                "INTERSECT " +
                "SELECT FRIEND_ID " +
                "FROM USER_FRIENDS uf " +
                "WHERE USER_ID  = ?)", (rs, rowNum) -> makeUser(rs, allFriends), id, otherId);
    }
}
