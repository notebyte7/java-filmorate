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
        changeEmptyName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        changeEmptyName(user);
        return userStorage.update(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
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
            user.getFriendIds().add(friendId);
            friend.getFriendIds().add(id);
            userStorage.update(user);
            userStorage.update(friend);
        }
    }

    public void removeFriend(int id, int friendId) {
        User user = getUserById(id);
        User friend = getUserById(friendId);
        if (user != null && friend != null) {
            user.getFriendIds().remove(friendId);
            friend.getFriendIds().remove(id);
            userStorage.update(user);
            userStorage.update(friend);
        }
    }

    public Collection<User> getFriends(int id) {
        return getUserById(id).getFriendIds().stream()
                .map(i -> getUserById(i))
                .collect(Collectors.toList());
    }

    public Collection<User> commonFriends(int id, int otherId) {
        User user = getUserById(id);
        User other = getUserById(otherId);
        if (user != null && other != null) {
            Set<Integer> intersection = new HashSet<>(user.getFriendIds());
            Set<Integer> secondSet = new HashSet<>(other.getFriendIds());
            intersection.retainAll(secondSet);
            return intersection.stream()
                    .map(i -> getUserById(i))
                    .collect(Collectors.toList());
        } else {
            throw new UserNotFoundException("Невозможно найти общих друзей - один из пользователей не существует");
        }
    }
}
