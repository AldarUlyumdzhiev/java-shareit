package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock UserClient userClient;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UserController(userClient)).build();
    }

    /* ---------- POST /users ---------- */
    @Test
    void postOk() throws Exception {
        UserDto in = new UserDto();
        in.setName("TestUser1");
        in.setEmail("test1@mail.com");

        UserDto out = new UserDto();
        out.setId(1L);
        out.setName("TestUser1");
        out.setEmail("test1@mail.com");

        when(userClient.createUser(any())).thenReturn(ResponseEntity.ok(out));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    /* ---------- GET /users/{id} ---------- */
    @Test
    void getById() throws Exception {
        UserDto out = new UserDto();
        out.setId(5L);
        out.setName("Test");
        out.setEmail("t5@mail.com");
        when(userClient.getUser(5L)).thenReturn(ResponseEntity.ok(out));

        mvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("t5@mail.com"));
    }
}
