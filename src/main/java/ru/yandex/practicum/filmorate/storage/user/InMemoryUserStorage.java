package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int uid;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        int id = generateId();
        user.setId(id);
        users.put(id, user);
        log.debug("Пользователь добавлен");
        return user;
    }

    @Override
    public User update(User user) {
        int id = user.getId();
        if (users.containsKey(id)) {
            users.put(id, user);
            log.debug("Пользователь обновлен");
        } else {
            log.debug("Пользователь для обновления не найден");
            throw new NotFoundException("Пользователь для обновления не найден");
        }
        return user;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User getUserById(int id) {
        if (users.containsKey(id)) {
            return (users.get(id));
        } else {
            throw new NotFoundException("Пользователь с " + id + " не найден");
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        getUserById(userId).getFriendIds().add(friendId);
        getUserById(friendId).getFriendIds().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserById(userId).getFriendIds().add(friendId);
        getUserById(friendId).getFriendIds().add(userId);
    }

    @Override
    public List<User> getFriends(int id) {
        return getUserById(id).getFriendIds().stream()
                .map(i -> getUserById(id))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> commonFriends(int id, int otherId) {
        User user = getUserById(id);
        User other = getUserById(otherId);
        Set<Integer> intersection = new HashSet<>(user.getFriendIds());
        Set<Integer> secondSet = new HashSet<>(other.getFriendIds());
        intersection.retainAll(secondSet);
        return intersection.stream()
                .map(i -> getUserById(i))
                .collect(Collectors.toList());
    }

    private int generateId() {
        return ++uid;
    }
}
