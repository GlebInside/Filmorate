package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {
    @Autowired
    @Qualifier("UserDbStorage")
    private UserStorage userStorage;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    public static User getValidUser() {
        var user = new User();
        user.setName("qwe");
        user.setEmail("1@mail.com");
        user.setLogin("vvv");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    public void shouldReturnEmptyArray() throws Exception {
        this
                .mockMvc
                .perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[]"))
                .andExpect(content().json("[]"));
    }

    @Test
    public void testPost() throws Exception {
        User user = getValidUser();
        ResultActions resultActions = this
                .mockMvc
                .perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        var stringContent = resultActions.andReturn().getResponse().getContentAsString();

        var r = objectMapper.readValue(stringContent, User.class);
        user.setId(r.getId());
        assertEquals(user, r);

        this
                .mockMvc
                .perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[" + objectMapper.writeValueAsString(user) + "]"));

    }

    @Test
    public void testPut() throws Exception {
        User user = getValidUser();
        user = userStorage.addUser(user);
        user.setLogin("ddd");
        this
                .mockMvc
                .perform(put("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(user)));

        this
                .mockMvc
                .perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("[" + objectMapper.writeValueAsString(user) + "]"));
    }

    @Test
    public void validationTest() throws Exception {
        var user = getValidUser();
        user.setLogin("");
        testStatus(user, status().is4xxClientError());

        user = getValidUser();
        user.setLogin(null);
        testStatus(user, status().is4xxClientError());

        user = getValidUser();
        user.setLogin("fd ds");
        testStatus(user, status().is4xxClientError());

        user = getValidUser();
        user.setEmail("");
        testStatus(user, status().is4xxClientError());

        user = getValidUser();
        user.setEmail("ds1.ru");
        testStatus(user, status().is4xxClientError());

        user = getValidUser();
        user.setBirthday(LocalDate.of(3000, 12, 28));
        testStatus(user, status().is4xxClientError());

    }

    private void testStatus(User user, ResultMatcher resultMatcher) throws Exception {
        this
                .mockMvc
                .perform(post("/films")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(resultMatcher);
    }
}
