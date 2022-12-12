package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private int uid;
    private final Map<Integer, User> users = new HashMap<>();

    public User create(User user) {
        int id = generateId();
        user.setId(id);
        changeEmptyName(user);
        users.put(id, user);
        log.debug("Пользователь добавлен");
        return user;
    }

    public User update(User user) {
        int id = user.getId();
        if (users.containsKey(id)) {
            changeEmptyName(user);
            users.put(id, user);
            log.debug("Пользователь обновлен");
        } else {
            log.debug("Пользователь для обновления не найден");
            throw new UserNotFoundException("Пользователь для обновления не найден");
        }
        return user;
    }

    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User getUserById(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new UserNotFoundException("Пользователь с " + id + " не найден");
        }
    }

    private int generateId() {
        return ++uid;
    }

    private void changeEmptyName(User user) {
        if (user.getName() == null || user.getName().equals("")) {
            user.setName(user.getLogin());
        }
    }
}
