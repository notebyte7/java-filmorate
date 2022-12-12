package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(id);
            userStorage.update(user);
            userStorage.update(friend);
        }
    }

    public void removeFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(id);
            userStorage.update(user);
            userStorage.update(friend);
        }
    }

    public Collection<User> getFriends(int id) {
        return getUserById(id).getFriends().stream()
                .map(i -> getUserById(i))
                .collect(Collectors.toList());
    }

    public Collection<User> commonFriends(int id, int otherId) {
        User user = getUserById(id);
        User other = getUserById(otherId);
        if (user != null && other != null) {
            Set<Integer> intersection = new HashSet<>(user.getFriends());
            Set<Integer> secondSet = new HashSet<>(other.getFriends());
            intersection.retainAll(secondSet);
            return intersection.stream()
                    .map(i -> getUserById(i))
                    .collect(Collectors.toList());
        } else {
            throw new UserNotFoundException("Невозможно найти общих друзей - один из пользователей не существует");
        }
    }
}
