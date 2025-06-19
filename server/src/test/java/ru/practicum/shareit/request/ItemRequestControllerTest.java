package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean  private ItemRequestService requestService;


    // POST /requests
    @Test
    void createRequest() throws Exception {
        CreateItemRequestDto req = new CreateItemRequestDto();
        req.setDescription("Нужна дрель");

        ItemRequestDto resp = ItemRequestDto.builder()
                .id(3L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        when(requestService.create(ArgumentMatchers.any(), ArgumentMatchers.eq(1L)))
                .thenReturn(resp);

        mvc.perform(post("/requests")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }


    // GET /requests
    @Test
    void getOwnRequests() throws Exception {
        when(requestService.getAllRequestsByUser(2L))
                .thenReturn(List.of(new ItemRequestDto(), new ItemRequestDto()));

        mvc.perform(get("/requests").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    // GET /requests/all
    @Test
    void getAllUserRequests() throws Exception {
        when(requestService.getAllUserRequests(2L, 0, 10))
                .thenReturn(List.of(new ItemRequestDto()));

        mvc.perform(get("/requests/all?from=0&size=10").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


    // GET /requests/{id}
    @Test
    void getRequestById() throws Exception {
        ItemRequestDto resp = ItemRequestDto.builder()
                .id(4L)
                .description("Газонокосилка")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getById(4L, 3L)).thenReturn(resp);

        mvc.perform(get("/requests/4").header(HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.description").value("Газонокосилка"));
    }
}
