package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean  private ItemService itemService;

    // POST /items
    @Test
    void createItem() throws Exception {
        ItemDto req = new ItemDto();
        req.setName("Дрель");
        req.setDescription("Ударная");
        req.setAvailable(true);

        ItemDto resp = new ItemDto();
        resp.setId(7L);
        resp.setName("Дрель");
        resp.setAvailable(true);

        when(itemService.create(ArgumentMatchers.any(), ArgumentMatchers.eq(1L)))
                .thenReturn(resp);

        mvc.perform(post("/items")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    // PATCH /items/{id}
    @Test
    void updateItem() throws Exception {
        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("Перфоратор");

        ItemDto resp = new ItemDto();
        resp.setId(3L);
        resp.setName("Перфоратор");

        when(itemService.update(3L, patch, 1L)).thenReturn(resp);

        mvc.perform(patch("/items/3")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Перфоратор"));
    }

    // GET /items/{id}
    @Test
    void getItemById() throws Exception {
        ItemWithBookingsDto resp = new ItemWithBookingsDto();
        resp.setId(4L);
        resp.setName("Лобзик");

        when(itemService.findById(4L, 1L)).thenReturn(resp);

        mvc.perform(get("/items/4").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Лобзик"));
    }

    // POST /items/{id}/comment
    @Test
    void addComment() throws Exception {
        CommentRequestDto req = new CommentRequestDto();
        req.setText("Отличная вещь");

        CommentResponseDto resp = new CommentResponseDto();
        resp.setId(10L);
        resp.setText("Отличная вещь");

        when(itemService.createComment(5L, req, 2L)).thenReturn(resp);

        mvc.perform(post("/items/5/comment")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.text").value("Отличная вещь"));
    }

    // GET /items
    @Test
    void getItemsByOwner() throws Exception {
        when(itemService.findAllByOwner(1L))
                .thenReturn(List.of(new ItemWithBookingsDto(), new ItemWithBookingsDto()));

        mvc.perform(get("/items").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // GET /items/search
    @Test
    void searchItems() throws Exception {
        ItemDto found = new ItemDto();
        found.setId(11L);
        found.setName("Ключ");

        when(itemService.search("Клю")).thenReturn(List.of(found));

        mvc.perform(get("/items/search?text=Клю"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ключ"));
    }
}
