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
        final String sqlQuery = "insert into users(email, birthday, name, login) values (?, ?, ?, ?)";
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
        final String sql = "update users set email= ?, birthday = ?, name = ?, login = ? where id = ?";
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
        final String sql = "select * from users where id = ?";
        var users = jdbcTemplate.query(sql, this::mapRowToUser, userId);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }

    @Override
    public User getById(int userId) {
        final String sql = "select * from users where id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToUser, userId);
    }

    @Override
    public void addFriend(int from, int to) {
        final String sql = "insert into friendship_requests (from_id, to_id) values (?, ?)";
        jdbcTemplate.update(sql, from, to);
    }

    @Override
    public void deleteFriend(Integer from, Integer to) {
        final String sql = "delete from friendship_requests where from_id = ? and to_id = ?";
        jdbcTemplate.update(sql, from, to);
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
        final String sql = "select * from friendship_requests where from_id = ? ";
        var rows = jdbcTemplate.query(sql,
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
