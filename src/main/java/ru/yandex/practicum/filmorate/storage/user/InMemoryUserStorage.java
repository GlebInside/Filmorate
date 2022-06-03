package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private Map<Integer, User> users = new HashMap<>();

    public Map<Integer, User> getUsers() {
        return users;
    }

    public void setUsers(Map<Integer, User> users) {
        this.users = users;
    }

    private int lastId = 1;

    @Override
    public Collection<User> getAllUsers() {
        log.debug(users.toString().toUpperCase());
        log.debug("users {} has been added", users.toString().toUpperCase());
        return users.values();
    }

    @Override
    public User addUser(User user) {
        validate(user);
        user.setId(lastId++);
        users.put(user.getId(), user);
        log.debug("user {} has been added", users.toString().toUpperCase());
        return user;
    }

    @Override
    public void updateUser(User user) {
        validate(user);
        getById(user.getId());
        users.put(user.getId(), user);
        log.debug("users {} has been updated", users.toString().toUpperCase());
    }

    @Override
    public User getById(int userId) {
        if (!users.containsKey(userId)) {
            throw new NoSuchElementException();
        }
        return users.get(userId);
    }

    @Override
    public void addFriend(int from, int to) {
        throw new NoSuchElementException();
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        throw new NoSuchElementException();
    }

    @Override
    public Optional<User> findUserById(int userId) {
        return Optional.empty();
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().contains(" ") || !(user.getEmail().contains("@"))) {
            throw new ValidationException("The user email must include @, should be without spaces " +
                    "and shouldn't be blank");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("The user login can't be empty or contains spaces");
        }
        if (user.getName() == null || user.getName().equals("")) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("The user birthday can't be after " + LocalDate.now());
        }
    }
}
