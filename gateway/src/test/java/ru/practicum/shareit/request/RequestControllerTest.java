package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(RequestController.class)
class RequestControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private RequestClient requestClient;


    // post /requests
    @Test
    void postOk() throws Exception {
        CreateItemRequestDto in = new CreateItemRequestDto();
        in.setDescription("Нужна дрель");

        ItemRequestDto out = new ItemRequestDto();
        out.setId(1L);
        out.setDescription("Нужна дрель");
        out.setCreated(LocalDateTime.now());

        when(requestClient.createRequest(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(post("/requests")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }


    // post /requests — blank description ⇒ 400
    @Test
    void postBlankDescription() throws Exception {
        CreateItemRequestDto in = new CreateItemRequestDto(); // description null

        mvc.perform(post("/requests")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).createRequest(anyLong(), any());
    }


    // get /requests
    @Test
    void getOwn() throws Exception {
        when(requestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok(new ItemRequestDto[] { new ItemRequestDto() }));

        mvc.perform(get("/requests").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


    // get /requests/all?from=0&size=1
    @Test
    void getAllOthers() throws Exception {
        when(requestClient.getAllRequests(1L, 0, 1))
                .thenReturn(ResponseEntity.ok(new ItemRequestDto[] { new ItemRequestDto() }));

        mvc.perform(get("/requests/all")
                        .header(HDR, 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }


    // get /requests/{id}
    @Test
    void getById() throws Exception {
        ItemRequestDto out = new ItemRequestDto();
        out.setId(3L);
        out.setDescription("Газонокосилка");
        out.setCreated(LocalDateTime.now());

        when(requestClient.getRequestById(1L, 3L))
                .thenReturn(ResponseEntity.ok(out));

        mvc.perform(get("/requests/3").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.description").value("Газонокосилка"));
    }
}
