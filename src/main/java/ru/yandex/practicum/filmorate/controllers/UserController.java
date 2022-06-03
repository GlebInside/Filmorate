package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {


    private final UserStorage userStorage;
    private final UserService service;

    public UserController(UserStorage userDbStorage, UserService service) {
        this.userStorage = userDbStorage;
        this.service = service;
    }


    @EventListener(ApplicationReadyEvent.class)
    private void onStart() {
        System.out.println("started");
    }


    @GetMapping
    private Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @GetMapping("/{id}")
    private User getUser(@PathVariable("id") Integer id) {
        try {
            return userStorage.getById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    private User addUser(@Valid @RequestBody User user) {
        return userStorage.addUser(user);
    }

    @PutMapping
    private User updateUser(@RequestBody User user) {
        try {
            userStorage.updateUser(user);
            return user;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("{id}/friends/{friendId}")
    private User addFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        try {
            var user = userStorage.getById(userId);
            var friend = userStorage.getById(friendId);
            service.addFriend(user, friend);
            userStorage.updateUser(user);
            userStorage.updateUser(friend);
            return user;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}/friends/{friendId}")
    private User deleteFriend(@PathVariable("id") Integer userId, @PathVariable("friendId") Integer friendId) {
        try {
            var user = userStorage.getById(userId);
            var friend = userStorage.getById(friendId);
            service.deleteFriend(user, friend);
            userStorage.updateUser(user);
            userStorage.updateUser(friend);
            return user;
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{id}/friends")
    private Set<User> friends(@PathVariable("id") Integer userId) {
        try {
            var user = userStorage.getById(userId);
            var friends = user.getFriends();
            return friends.stream().map(userStorage::getById).collect(Collectors.toSet());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("{id}/friends/common/{otherId}")
    private Collection<User> commonFriends(@PathVariable("id") Integer userId, @PathVariable("otherId") Integer otherId) {
        try {
            var user1 = userStorage.getById(userId);
            var user2 = userStorage.getById(otherId);
            return service.getMutualFriends(user1, user2).stream().map(userStorage::getById).collect(Collectors.toSet());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
