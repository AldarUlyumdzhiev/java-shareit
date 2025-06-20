package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    private static final String HDR = "X-Sharer-User-Id";
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock RequestClient requestClient;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new RequestController(requestClient)).build();
    }

    @Test
    void postOk() throws Exception {
        CreateItemRequestDto in = new CreateItemRequestDto();
        in.setDescription("Нужна дрель");

        ItemRequestDto out = ItemRequestDto.builder()
                .id(1L).description("Нужна дрель").created(LocalDateTime.now()).build();

        when(requestClient.createRequest(eq(1L), any())).thenReturn(ResponseEntity.ok(out));

        mvc.perform(post("/requests").header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById() throws Exception {
        ItemRequestDto out = ItemRequestDto.builder()
                .id(3L).description("Газонокосилка").created(LocalDateTime.now()).build();

        when(requestClient.getRequestById(1L, 3L)).thenReturn(ResponseEntity.ok(out));

        mvc.perform(get("/requests/3").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }
}
