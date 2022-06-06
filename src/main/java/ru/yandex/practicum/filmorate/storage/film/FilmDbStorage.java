package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.validation.Valid;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@Repository
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {


    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<Film> getAllFilms() {
        return jdbcTemplate.query("select * from films", this::mapRowToFilm);
    }

    public Integer addFilm(@Valid Film film) {
        validate(film);
        final String sqlQuery = "insert into films(name, description, duration, release_date, mpa_id) values (?, ?, ?, ?, ?)";
        System.out.println(film.getReleaseDate());
        System.out.println(Date.valueOf(film.getReleaseDate()));
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setInt(3, film.getDuration());
            preparedStatement.setDate(4, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(5, film.getMpa().getId());

            return preparedStatement;
        }, keyHolder);
        return (Integer) keyHolder.getKey();
    }


    public void updateFilm(Film film) {
        getById(film.getId());
        validate(film);
        final String sql = "update films set name = ?, description = ?, duration = ?, release_date = ?, mpa_id = ? where id = ?";
        var r = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId()
        );

        log.debug("film {} has been updated", film.getName().toUpperCase());
    }

    @Override
    public Film getById(int filmId) {
        final String sql = "select * from films where id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        final String sql = "insert into likes (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        final String sql = "delete from likes where film_id = ? and user_id  = ?";
        var rowsAffected = jdbcTemplate.update(sql,
                filmId, userId);
        if (rowsAffected != 1) {
            throw new NoSuchElementException();
        }
    }

    private Film mapRowToFilm(ResultSet resultSet, int i) throws SQLException {
        var filmId = resultSet.getInt("id");
        var mpaId = resultSet.getInt("mpa_id");

        System.out.println(resultSet.getDate("release_date"));
        System.out.println(resultSet.getDate("release_date").toLocalDate());
        return Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .duration(resultSet.getInt("duration"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .mpa(getMpaById(mpaId))
                .likes(getLikes(filmId))
                .build();
    }

    private Set<Integer> getLikes(int filmId) {
        final String sql = "select user_id from likes where film_id = ?";
        var rows = jdbcTemplate.query(
                sql,
                (rs, i) -> rs.getInt("user_id"),
                filmId);
        return new HashSet<>(rows);
    }

    private Mpa getMpaById(int mpa_id) {
        final String sql = "select * from mpa where id = ?";
        return jdbcTemplate.queryForObject(sql,
                (resultSet, i) -> new Mpa(resultSet.getInt("id"), resultSet.getString("name")), mpa_id);
    }


    private void validate(Film film) {
        if (film.getName().equals("")) {
            throw new ValidationException("The film name should be added");
        }
        if (film.getDescription().equals("")) {
            throw new ValidationException("The film description should be greater than 0");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("The film description should be be less then 200 symbols");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("The release date should be after 1985-12-28");
        }
        if (film.getDuration() < 1) {
            throw new ValidationException("Film duration should be positive ");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("MPA rate is not provided");
        }
    }
}
