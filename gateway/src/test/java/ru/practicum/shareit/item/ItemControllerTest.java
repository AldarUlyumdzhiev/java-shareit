package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private ItemClient itemClient;


    // POST /items
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

        mvc.perform(post("/items")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }


    // POST /items
    @Test
    void postBlankName() throws Exception {
        ItemDto in = new ItemDto();
        in.setDescription("No name");
        in.setAvailable(true);

        mvc.perform(post("/items")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any());
    }


    // PATCH /items/{id}
    @Test
    void patchOk() throws Exception {
        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setDescription("Новое описание");

        ItemDto out = new ItemDto();
        out.setId(3L);
        out.setName("Старое имя");
        out.setDescription("Новое описание");
        out.setAvailable(true);

        when(itemClient.updateItem(eq(2L), eq(3L), any()))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(patch("/items/3")
                        .header(HDR, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }


    // PATCH /items/{id}
    @Test
    void patchNoFields() throws Exception {
        ItemUpdateDto empty = new ItemUpdateDto();

        mvc.perform(patch("/items/5")
                        .header(HDR, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(empty)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any());
    }


    // GET /items/{id}
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
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Лобзик"));
    }


    // GET /items
    @Test
    void getItems() throws Exception {
        when(itemClient.getItems(4L))
                .thenReturn(ResponseEntity.ok(new ItemDto[] { new ItemDto(), new ItemDto() }));

        mvc.perform(get("/items").header(HDR, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    // GET /items/search
    @Test
    void searchItems() throws Exception {
        ItemDto out = new ItemDto();
        out.setId(9L);
        out.setName("Ключ");
        out.setDescription("набор");

        when(itemClient.search("ключ"))
                .thenReturn(ResponseEntity.ok(new ItemDto[] { out }));

        mvc.perform(get("/items/search").param("text", "ключ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(9));
    }


    // POST /items/{id}/comment
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

        mvc.perform(post("/items/6/comment")
                        .header(HDR, 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь"));
    }


    // POST /items/{id}/comment
    @Test
    void postCommentBlank() throws Exception {
        CommentRequestDto req = new CommentRequestDto(); // text null

        mvc.perform(post("/items/8/comment")
                        .header(HDR, 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createComment(anyLong(), anyLong(), any());
    }
}
