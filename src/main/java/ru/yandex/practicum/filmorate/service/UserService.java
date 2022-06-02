package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class UserService {

    public Collection<Integer> getMutualFriends(User user, User user2) {
        Collection<Integer> mutualFriends = new HashSet<>();
        for (int u : user.getFriends()) {
            for (int u2 : user2.getFriends()) {
                if (u == u2) {
//                    if (u == u2 && u2 != user.getId() && u2 != user2.getId()) {
                    mutualFriends.add(u);
                }
            }
        }
        return mutualFriends;
    }

    public void addFriend(User user, User user2) {
        if (!(user.getFriends().contains(user2.getId()))) {
            user.getFriends().add(user2.getId());
//            user2.getFriends().add(user.getId());
        } else {
            log.error("User " + user + " is in your friends list already");
        }
    }

    public void deleteFriend(User user, User user2) {
        if (!user.getFriends().contains(user2.getId())) {
            throw new NoSuchElementException();
        }
        user.getFriends().remove(user2.getId());
        user2.getFriends().remove(user.getId());
    }
}
