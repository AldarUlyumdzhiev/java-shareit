package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class BookingDtoJsonTest {

    @Autowired private ObjectMapper mapper;

    @Test
    void entityToDtoAndBack() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030,1,1,12,0);
        LocalDateTime end   = start.plusDays(1);

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(5L);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setStatus(Status.WAITING);

        BookingResponseDto.ItemInfo item = new BookingResponseDto.ItemInfo();
        item.setId(42L); item.setName("Перфоратор");
        dto.setItem(item);

        BookingResponseDto.BookerInfo booker = new BookingResponseDto.BookerInfo();
        booker.setId(7L);
        dto.setBooker(booker);

        // serialize
        String json = mapper.writeValueAsString(dto);
        assertThat(json)
                .contains("\"id\":5")
                .contains("\"status\":\"WAITING\"")
                .contains("\"start\":\"2030-01-01T12:00:00\"")
                .contains("\"item\":{\"id\":42,\"name\":\"Перфоратор\"}");

        // deserialize
        BookingResponseDto back = mapper.readValue(json, BookingResponseDto.class);
        assertThat(back.getStart()).isEqualTo(start);
        assertThat(back.getItem().getName()).isEqualTo("Перфоратор");
    }
}
