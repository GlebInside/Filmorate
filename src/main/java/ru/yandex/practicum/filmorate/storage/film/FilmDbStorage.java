package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.validation.Valid;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

//@Component
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
        var films = jdbcTemplate.query("select * from films", this::mapRowToFilm);
        return films;
    }






    public Integer addFilm(@Valid Film film) {
        validate(film);
        String sqlQuery = "insert into films(name, description, duration, release_date, mpa_id) values (?, ?, ?, ?, ?)";
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
        var sql = "update films set name = ?, description = ?, duration = ?, release_date = ?, mpa_id = ?";
        sql += "\nwhere id = ?";
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
        var film = jdbcTemplate.queryForObject("select * from films where id = ?", this::mapRowToFilm, filmId);


//        if (!films.containsKey(filmId)) {
//            throw new NoSuchElementException();
//        }
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        var like = Like.builder().film_id(filmId).user_id(userId).build();
        jdbcTemplate.update("insert into likes (film_id, user_id) values (?, ?)",
                filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        var rowsAffected=  jdbcTemplate.update("delete from likes where film_id = ? and user_id  = ?",
                filmId, userId);
        if (rowsAffected != 1){
            throw new NoSuchElementException();
        }
    }

    private Film mapRowToFilm(ResultSet resultSet, int i) throws SQLException {
        var filmId = resultSet.getInt("id");
        var mpa_id = resultSet.getInt("mpa_id");
        return Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .duration(resultSet.getInt("duration"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .mpa(getMpaById(mpa_id))
                .likes(getLikes(filmId))
                .build();
    }

    private Set<Integer> getLikes(int filmId) {
        var rows = jdbcTemplate.query(
                "select user_id from likes where film_id = ?",
                (rs,i)->rs.getInt("user_id"),
                filmId);
        return new HashSet<>(rows);
    }

    private Mpa getMpaById(int mpa_id) {
        var mpa = jdbcTemplate.queryForObject("select * from mpa where id = ?",
                (resultSet, i) -> new Mpa(resultSet.getInt("id"), resultSet.getString("name")), mpa_id);
        return mpa;
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
