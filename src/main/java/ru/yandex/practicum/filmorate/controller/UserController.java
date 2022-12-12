package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        log.debug("Получен запрос POST /users - создание User");
        return userService.create(user);
    }

    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        log.debug("Получен запрос PUT /users - обновление User");
        return userService.update(user);
    }

    @GetMapping(value = "/users")
    public Collection<User> getUsers() {
        log.debug("Получен запрос GET /users - получить все User");
        return userService.getUsers();
    }

    @GetMapping(value = "/users/{id}")
    public User getUserById(@PathVariable int id) {
        log.debug("Получен запрос GET /users/id - получить User по id");
        return userService.getUserById(id);
    }

    @PutMapping(value = "/users/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.debug("Получен запрос PUT /users/{id}/friends/{friendId} - добавить пользователю с id друга с friendId");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping(value = "/users/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        log.debug("Получен запрос DELETE /users/{id}/friends/{friendId} - удалить пользователю с id друга с friendId");
        userService.removeFriend(id, friendId);
    }

    @GetMapping(value = "/users/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {
        log.debug("Получен запрос GET /users/{id}/friends - список друзей пользователя с id");
        return userService.getFriends(id);
    }

    @GetMapping(value = "/users/{id}/friends/common/{otherId}")
    public Collection<User> commonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.debug("Получен запрос GET /users/{id}/friends/common/{otherId} - " +
                "список общих друзей пользователей с id и otherId");
        return userService.commonFriends(id, otherId);
    }
}
