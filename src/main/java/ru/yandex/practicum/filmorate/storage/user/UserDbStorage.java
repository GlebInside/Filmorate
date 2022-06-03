package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<User> getAllUsers() {
        return jdbcTemplate.query("select * from users", this::mapRowToUser);
    }

    public User addUser(@Valid User user) {
        validate(user);
        String sqlQuery = "insert into users(email, birthday, name, login) values (?, ?, ?, ?)";
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setDate(2, Date.valueOf(user.getBirthday()));
            preparedStatement.setString(3, user.getName());
            preparedStatement.setString(4, user.getLogin());


            return preparedStatement;
        }, keyHolder);
        return getById((Integer) keyHolder.getKey());
    }

    public void updateUser(User user) {
        getById(user.getId());
        validate(user);
        var sql = "update users set email= ?, birthday = ?, name = ?, login = ?";
        sql += "\nwhere id = ?";
        var r = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getBirthday(),
                user.getName(),
                user.getLogin(),
                user.getId()
        );
    }

    @Override
    public Optional<User> findUserById(int userId) {
        var users = jdbcTemplate.query("select * from users where id = ?", this::mapRowToUser, userId);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    @Override
    public User getById(int userId) {
        return jdbcTemplate.queryForObject("select * from users where id = ?", this::mapRowToUser, userId);
    }

    @Override
    public void addFriend(int from, int to) {
        jdbcTemplate.update("insert into friendship_requests (from_id, to_id) values (?, ?)", from, to);
    }

    @Override
    public void deleteFriend(Integer from, Integer to) {
        jdbcTemplate.update("delete from friendship_requests where from_id = ? and to_id = ?", from, to);
    }

    private User mapRowToUser(ResultSet resultSet, int i) throws SQLException {
        var userId = resultSet.getInt("id");

        return User.builder()
                .id(userId)
                .name(resultSet.getString("name"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .login(resultSet.getString("login"))
                .friends(loadFriends(userId))
                .build();
    }

    private Set<Integer> loadFriends(int userId) {
        var rows = jdbcTemplate.query("select * from friendship_requests where from_id = ? ",
                (resultSet, i) -> resultSet.getInt("to_id"), userId);
        return new HashSet<>(rows);
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
