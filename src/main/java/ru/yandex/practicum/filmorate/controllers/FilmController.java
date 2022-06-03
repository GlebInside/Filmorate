package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService service;

    @Autowired
    public FilmController(@Qualifier("FilmDbStorage") FilmStorage filmStorage, FilmService service) {
        this.filmStorage = filmStorage;
        this.service = service;
    }

    @GetMapping
    private Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @PostMapping
    private Film addFilm(@Valid @RequestBody Film film) {
        var film_id = filmStorage.addFilm(film);
        return filmStorage.getById(film_id);
    }

    @GetMapping("/{id}")
    private Film getFilm(@PathVariable("id") Integer id) {
        try {
            return filmStorage.getById(id);
        } catch (NoSuchElementException | EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping
    private Film updateFilm(@Valid @RequestBody Film film) {
        try {
            filmStorage.updateFilm(film);
            return filmStorage.getById(film.getId());
        } catch (NoSuchElementException | EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("{id}/like/{userId}")
    private void addLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        service.addLike(filmId, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    private void deleteLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        try {
            service.deleteLike(filmId, userId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("popular")
    private Collection<Film> getMostPopular(@RequestParam(name = "count", required = false) Integer count) {
        if (count == null) {
            count = 10;
        }
        var films = service.getMostPopular(count).collect(Collectors.toUnmodifiableList());
        return films;
    }
}
