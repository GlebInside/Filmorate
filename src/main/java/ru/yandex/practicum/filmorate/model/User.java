package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private final Set<Integer> friends = new HashSet<>();

    private int id;

    @Email
    private String email;
    @NotBlank
    private String login;
    private String name;
    private LocalDate birthday;


}
