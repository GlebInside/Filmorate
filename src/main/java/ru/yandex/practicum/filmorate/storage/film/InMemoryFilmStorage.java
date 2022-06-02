package ru.yandex.practicum.filmorate.storage.film;

import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Slf4j
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Integer, Film> films = new HashMap<>();

    public InMemoryFilmStorage() {
    }


    public Collection<Film> getAllFilms() {
        return films.values();
    }

    public void setFilms(Map<Integer, Film> films) {
        this.films = films;
    }

    private int lastId = 1;

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }


    public Integer addFilm(@Valid Film film) {
//        if (film.getMpa().getId()==5)
        film.getMpa().setName("todo: remove");
        validate(film);
        film.setId(lastId++);
        films.put(film.getId(), film);
        log.debug("film {} has been added", film.getName().toUpperCase());
        return  film.getId();
    }


    public void updateFilm(Film film) {
        film.getMpa().setName("todo: remove");
        getById(film.getId());
        validate(film);
        films.put(film.getId(), film);
        log.debug("film {} has been updated", film.getName().toUpperCase());
    }

    @Override
    public Film getById(int filmId) {
        if (!films.containsKey(filmId)) {
            throw new NoSuchElementException();
        }
        return films.get(filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        throw new NotYetImplementedException();
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        throw new NotYetImplementedException();

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
