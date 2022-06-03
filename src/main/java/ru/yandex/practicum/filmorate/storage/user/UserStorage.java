package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    public Collection<User> getAllUsers();

    public User addUser(User user);

    public void updateUser(User user);

    Optional<User> findUserById(int userId);

    User getById(int userId);
}
