package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock ItemClient itemClient;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ItemController(itemClient))
                .build();
    }

    @Test
    void postOk() throws Exception {
        ItemDto in = new ItemDto();
        in.setName("Дрель");
        in.setDescription("Ударная");
        in.setAvailable(true);

        ItemDto out = new ItemDto();
        out.setId(1L);
        out.setName("Дрель");
        out.setDescription("Ударная");
        out.setAvailable(true);

        when(itemClient.createItem(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(post("/items").header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patchOk() throws Exception {
        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setDescription("Новое описание");

        ItemDto out = new ItemDto();
        out.setId(3L);
        out.setName("Дрель");
        out.setDescription("Новое описание");
        out.setAvailable(true);

        when(itemClient.updateItem(eq(2L), eq(3L), any()))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(patch("/items/3").header(HDR, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    @Test
    void getItem() throws Exception {
        ItemDto out = new ItemDto();
        out.setId(7L);
        out.setName("Лобзик");
        out.setDescription("d");
        out.setAvailable(true);

        when(itemClient.getItem(1L, 7L)).thenReturn(ResponseEntity.ok(out));

        mvc.perform(get("/items/7").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void postCommentOk() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setText("Отличная вещь");

        CommentResponseDto resp = new CommentResponseDto();
        resp.setId(1L);
        resp.setText("Отличная вещь");
        resp.setAuthorName("TestUser");
        resp.setCreated(Instant.now());

        when(itemClient.createComment(eq(3L), eq(6L), any()))
                .thenReturn(ResponseEntity.ok(resp));

        mvc.perform(post("/items/6/comment").header(HDR, 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь"));
    }
}
