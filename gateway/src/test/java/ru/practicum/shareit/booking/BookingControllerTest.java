package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String HDR = "X-Sharer-User-Id";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired private MockMvc      mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean private BookingClient bookingClient;


    // POST /bookings
    @Test
    void postOk() throws Exception {
        BookItemRequestDto in = new BookItemRequestDto(
                10L,
                NOW.plusDays(1),
                NOW.plusDays(2));

        Map<String, Object> body = Map.of(
                "id",     1,
                "status", "WAITING");

        when(bookingClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(body));

        mvc.perform(post("/bookings")
                        .header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }


    // POST /bookings — start in past ⇒ 400
    @Test
    void postPastStart() throws Exception {
        BookItemRequestDto bad = new BookItemRequestDto(
                3L,
                NOW.minusHours(1),
                NOW.plusHours(5));

        mvc.perform(post("/bookings")
                        .header(HDR, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }


    // PATCH /bookings/{id}?approved
    @Test
    void approveOk() throws Exception {
        Map<String, Object> body = Map.of("status", "APPROVED");

        when(bookingClient.approve(5L, 4L, true))
                .thenReturn(ResponseEntity.ok(body));

        mvc.perform(patch("/bookings/5")
                        .header(HDR, 4)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }


    // GET /bookings/{id}
    @Test
    void getBooking() throws Exception {
        Map<String, Object> body = Map.of("id", 8, "status", "WAITING");

        when(bookingClient.get(8L, 1L))
                .thenReturn(ResponseEntity.ok(body));

        mvc.perform(get("/bookings/8").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }


    // GET /bookings?state=ALL
    @Test
    void getAllByUser() throws Exception {
        when(bookingClient.getBookings(7L))
                .thenReturn(ResponseEntity.ok(new Object[] { Map.of("id", 1), Map.of("id", 2) }));

        mvc.perform(get("/bookings")
                        .header(HDR, 7)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    // GET /bookings/owner?state=PAST
    @Test
    void getAllByOwner() throws Exception {
        when(bookingClient.getAllByOwner(6L, BookingState.PAST))
                .thenReturn(ResponseEntity.ok(new Object[] { Map.of("id", 11) }));

        mvc.perform(get("/bookings/owner")
                        .header(HDR, 6)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11));
    }


    // GET /bookings?state=UNKNOWN ⇒ 400
    @Test
    void getAllBadState() throws Exception {
        mvc.perform(get("/bookings")
                        .header(HDR, 5)
                        .param("state", "SOME"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong());
    }
}
