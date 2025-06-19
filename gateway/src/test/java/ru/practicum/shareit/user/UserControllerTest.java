package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private UserClient userClient;


    // post /users
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test1@mail.com"));
    }


    // post /users
    @Test
    void postBadEmail() throws Exception {
        UserDto in = new UserDto();
        in.setName("BadUser");
        in.setEmail("wrong");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any());
    }


    // Patch /users/{id}
    @Test
    void patchOk() throws Exception {
        UserDto patch = new UserDto();
        patch.setName("NewName");

        UserDto out = new UserDto();
        out.setId(2L);
        out.setName("NewName");
        out.setEmail("u2@mail.com");

        when(userClient.updateUser(eq(2L), any())).thenReturn(ResponseEntity.ok(out));

        mvc.perform(patch("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }


    // get /users
    @Test
    void getAllUsers() throws Exception {
        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(new UserDto[] { new UserDto(), new UserDto() }));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    // get /users/{id}
    @Test
    void getUserById() throws Exception {
        UserDto out = new UserDto();
        out.setId(5L);
        out.setName("TestUser5");
        out.setEmail("t5@mail.com");

        when(userClient.getUser(5L)).thenReturn(ResponseEntity.ok(out));

        mvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("t5@mail.com"));
    }


    // delete /users/{id}
    @Test
    void deleteOk() throws Exception {
        when(userClient.deleteUser(9L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/9"))
                .andExpect(status().isOk());

        verify(userClient).deleteUser(9L);
    }
}
