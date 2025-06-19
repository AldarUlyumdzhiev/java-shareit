package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockBean  private BookingService bookingService;

    // POST /bookings
    @Test
    void createBooking() throws Exception {
        BookingRequestDto req = new BookingRequestDto();
        req.setItemId(10L);
        req.setStart(LocalDateTime.now().plusDays(1));
        req.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto resp = new BookingResponseDto();
        resp.setId(1L);
        resp.setStatus(Status.WAITING);

        when(bookingService.create(ArgumentMatchers.any(), ArgumentMatchers.eq(1L)))
                .thenReturn(resp);

        mvc.perform(post("/bookings")
                        .header(HEADER, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    // GET /bookings/{id}
    @Test
    void getBookingById() throws Exception {
        BookingResponseDto resp = new BookingResponseDto();
        resp.setId(5L);
        resp.setStatus(Status.APPROVED);

        when(bookingService.getById(5L, 2L)).thenReturn(resp);

        mvc.perform(get("/bookings/5").header(HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // PATCH /bookings/{id}?approved=true
    @Test
    void approveBooking() throws Exception {
        BookingResponseDto resp = new BookingResponseDto();
        resp.setId(3L);
        resp.setStatus(Status.APPROVED);

        when(bookingService.approveBooking(3L, 1L, true)).thenReturn(resp);

        mvc.perform(patch("/bookings/3?approved=true").header(HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // GET /bookings
    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByUser(4L))
                .thenReturn(List.of(new BookingResponseDto()));

        mvc.perform(get("/bookings").header(HEADER, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // GET /bookings/owner
    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(4L, "ALL"))
                .thenReturn(List.of(new BookingResponseDto(), new BookingResponseDto()));

        mvc.perform(get("/bookings/owner").header(HEADER, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
