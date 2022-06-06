package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> getAllFilms();

    Integer addFilm(Film film);

    void updateFilm(Film film);

    Film getById(int filmId);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);
}
