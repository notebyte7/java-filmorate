package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {
    private int uid;
    private Map<Integer, User> users = new HashMap<>();

    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        log.debug("Получен запрос POST /users - создание User");
        int id = generateId();
        user.setId(id);
        if (user.getName() == "" || user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(id, user);
        log.debug("Пользователь добавлен");
        return user;
    }

    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        log.debug("Получен запрос PUT /users - обновление User");
        int id = user.getId();
        if (users.containsKey(id)) {
            if (user.getName() == "" || user.getName() == null) {
                user.setName(user.getLogin());
            }
            users.put(id, user);
            log.debug("Пользователь обновлен");
        } else {
            log.debug("Пользователь для обновления не найден");
            throw new UserNotFoundException("Пользователь для обновления не найден");
        }
        return user;
    }

    @GetMapping(value = "/users")
    public List<User> getUsers() {
        log.debug("Получен запрос GET /users - получить все User");
        return new ArrayList<>(users.values());
    }

    private int generateId() {
        return ++uid;
    }
}
