package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@AutoConfigureTestDatabase
@SpringBootTest
class FilmServiceTest {
    @Qualifier("UserDbStorage")
    @Autowired
    UserStorage userStorage;

    @Autowired
    @Qualifier("FilmDbStorage")
    private FilmStorage filmStorage;

    @Test
    public void testGetTop10() {
        createUsers(100);
        addFilms(filmStorage, 100);
        var service = new FilmService(filmStorage);
        var films = service.getMostPopular(10).collect(Collectors.toUnmodifiableList());
        assertEquals(10, films.size());
        assertTrue(films.stream().allMatch(x -> x.obtainLikesCount() > 88));
    }

    private void createUsers(int n) {
        for (int i = 0; i < n; i++) {
            var user = new User();
            user.setName("user" + i);
            user.setEmail("1@mail");
            user.setLogin("login");
            user.setBirthday(LocalDate.now());
            userStorage.addUser(user);
        }
    }

    private void addFilms(FilmStorage storage, int count) {
        for (int i = 0; i < count; i++) {
            var film = Film.builder()
                    .name("film" + i)
                    .description("descr" + i)
                    .releaseDate(LocalDate.now())
                    .duration(i + 1)
                    .mpa(new Mpa(1, "G"))
                    .build();
            for (int j = 1; j < i; j++) {
                storage.addLike(i, j);
            }
            storage.addFilm(film);
        }
    }
}