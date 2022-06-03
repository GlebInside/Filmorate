package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class FilmControllerTest {

    @Autowired
    @Qualifier("FilmDbStorage")
    private FilmStorage filmStorage;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private Film getValidFilm() {
        var film = Film.builder()
                .name("qwe")
                .description("descr")
                .releaseDate(LocalDate.of(1900, 12, 1))
                .duration(10)
                .mpa(Mpa.G())
                .likes(new HashSet<>())
                .build();
        return film;
    }


    @Test
    public void shouldReturnEmptyArray() throws Exception {
        this
                .mockMvc
                .perform(get("/films"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"))
                .andExpect(content().json("[]"));
    }

    @Test
    public void testPost() throws Exception {
        Film film = getValidFilm();
        ResultActions resultActions = this
                .mockMvc
                .perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));


        var stringContent = resultActions.andReturn().getResponse().getContentAsString();

        var r = objectMapper.readValue(stringContent, Film.class);
        film.setId(r.getId());
        assertEquals(film, r);

        this
                .mockMvc
                .perform(get("/films"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[" + objectMapper.writeValueAsString(film) + "]"));
    }

    @Test
    public void testPut() throws Exception {
        var film = getValidFilm();
        var filmId = filmStorage.addFilm(film);
        film.setId(filmId);
        film.setDescription("ddd");
        this
                .mockMvc
                .perform(put("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(film)));

        this
                .mockMvc
                .perform(get("/films"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[" + objectMapper.writeValueAsString(film) + "]"));

    }

    @Test
    public void validationTest() throws Exception {
        var film = getValidFilm();
        film.setName("");
        testStatus(film, status().is4xxClientError());

        film = getValidFilm();
        film.setName(null);
        testStatus(film, status().is4xxClientError());

        film = getValidFilm();
        film.setDuration(-1);
        testStatus(film, status().is4xxClientError());

        film = getValidFilm();
        film.setReleaseDate(LocalDate.of(1800, 12, 28));
        testStatus(film, status().is4xxClientError());

    }

    private void testStatus(Film film, ResultMatcher resultMatcher) throws Exception {
        this
                .mockMvc
                .perform(post("/films")
                        .content(objectMapper.writeValueAsString(film))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(resultMatcher);
    }
}