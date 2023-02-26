package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        changeEmptyName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        changeEmptyName(user);
        User newUser = userStorage.update(user);
        if (newUser != null) {
            return newUser;
        } else {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        User newUser = userStorage.getUserById(id);
        if (newUser != null) {
            return newUser;
        } else {
            throw new NotFoundException("Пользователь не существует");
        }
    }

    private void changeEmptyName(User user) {
        if (user.getName() == null || user.getName().equals("")) {
            user.setName(user.getLogin());
        }
    }

    public void addFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (user != null && friend != null) {
            userStorage.addFriend(id, friendId);
        }
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user != null && friend != null) {
            userStorage.removeFriend(userId, friendId);
        }
    }

    public Collection<User> getFriends(int id) {
        return userStorage.getFriends(id);
    }

    public Collection<User> commonFriends(int id, int otherId) {
        User user = getUserById(id);
        User other = getUserById(otherId);
        if (user != null && other != null) {
            return userStorage.commonFriends (id, otherId);
        } else {
            throw new NotFoundException("Невозможно найти общих друзей - один из пользователей не существует");
        }
    }
}
