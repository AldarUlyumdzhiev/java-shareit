package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private static final String HDR = "X-Sharer-User-Id";
    private static final LocalDateTime NOW = LocalDateTime.now();

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Mock BookingClient bookingClient;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        BookingController controller = new BookingController(bookingClient);

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                // подключаем глобальный handler → IllegalArgumentException => 400
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postOk() throws Exception {
        BookItemRequestDto in = new BookItemRequestDto(
                10L, NOW.plusDays(1), NOW.plusDays(2));

        when(bookingClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "WAITING")));

        mvc.perform(post("/bookings").header(HDR, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void postPastStart() throws Exception {
        BookItemRequestDto bad = new BookItemRequestDto(
                3L, NOW.minusHours(1), NOW.plusHours(5));

        mvc.perform(post("/bookings").header(HDR, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approveOk() throws Exception {
        when(bookingClient.approve(5L, 4L, true))
                .thenReturn(ResponseEntity.ok(Map.of("status", "APPROVED")));

        mvc.perform(patch("/bookings/5").header(HDR, 4)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking() throws Exception {
        when(bookingClient.get(8L, 1L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 8, "status", "WAITING")));

        mvc.perform(get("/bookings/8").header(HDR, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void getAllByUser() throws Exception {
        when(bookingClient.getBookings(7L))
                .thenReturn(ResponseEntity.ok(new Object[]{Map.of("id", 1), Map.of("id", 2)}));

        mvc.perform(get("/bookings").header(HDR, 7).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingClient.getAllByOwner(6L, BookingState.PAST))
                .thenReturn(ResponseEntity.ok(new Object[]{Map.of("id", 11)}));

        mvc.perform(get("/bookings/owner").header(HDR, 6).param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    void getAllBadState() throws Exception {
        mvc.perform(get("/bookings").header(HDR, 5).param("state", "SOME"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: SOME"));

        verifyNoInteractions(bookingClient);
    }
}
