package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    @Test
    public void testGetTop10() {
        var users = createUsers(100);
        var filmStorage = new InMemoryFilmStorage();
        addFilms(filmStorage, 100, users);
        var service = new FilmService(filmStorage, null);
        var films = service.getMostPopular(10).collect(Collectors.toUnmodifiableList());
        assertEquals(10, films.size());
        assertTrue(films.stream().allMatch(x -> x.obtainLikesCount() > 89));
    }

    private List<User> createUsers(int n) {
        var list = new ArrayList<User>();
        for (int i = 0; i < n; i++) {
            var user = new User();
            user.setName("user" + i);
            user.setId(i);
            list.add(user);
        }
        return list;
    }

    private void addFilms(InMemoryFilmStorage storage, int count, List<User> users) {
        for (int i = 0; i < count; i++) {
            var film = Film.builder()
                    .id(i)
                    .name("film" + i)
            .releaseDate(LocalDate.now())
            .duration(i + 1)
            .mpa(Mpa.G())
                    .build();
            for (int j = 0; j < i; j++) {
                storage.addLike(i, j);
            }
            storage.addFilm(film);
        }
    }


}