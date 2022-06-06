package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.stream.Stream;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }


    public Stream<Film> getMostPopular(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((a, b) -> b.obtainLikesCount() - a.obtainLikesCount())
                .limit(count);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);

    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.deleteLike(filmId, userId);
    }
}
