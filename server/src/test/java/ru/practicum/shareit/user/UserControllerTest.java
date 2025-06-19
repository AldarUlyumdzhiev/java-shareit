package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean  private UserService userService;


    // POST /users
    @Test
    void createUser() throws Exception {
        UserDto req = new UserDto();
        req.setName("Bob");
        req.setEmail("bob@mail.ru");

        UserDto resp = new UserDto();
        resp.setId(1L);
        resp.setName("Bob");
        resp.setEmail("bob@mail.ru");

        when(userService.create(ArgumentMatchers.any())).thenReturn(resp);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("bob@mail.ru"));
    }


    // PATCH /users/{id}
    @Test
    void updateUser() throws Exception {
        UserDto patch = new UserDto();
        patch.setName("Bob");

        UserDto resp = new UserDto();
        resp.setId(2L);
        resp.setName("Bob");

        when(userService.update(2L, patch)).thenReturn(resp);

        mvc.perform(patch("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bob"));
    }


    // GET /users
    @Test
    void getAllUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(new UserDto(), new UserDto(), new UserDto()));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }


    // GET /users/{id}
    @Test
    void getUserById() throws Exception {
        UserDto resp = new UserDto();
        resp.setId(5L);
        resp.setEmail("user@mail.ru");

        when(userService.findById(5L)).thenReturn(resp);

        mvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("user@mail.ru"));
    }


    // DELETE /users/{id}
    @Test
    void deleteUser() throws Exception {
        mvc.perform(delete("/users/9"))
                .andExpect(status().isOk());
    }
}
