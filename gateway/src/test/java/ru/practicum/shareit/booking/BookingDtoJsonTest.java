package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class BookingDtoJsonTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired private ObjectMapper mapper;
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();


    // toJson
    @Test
    void toJson() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(
                7L,
                LocalDateTime.of(2030, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 2, 12, 0));

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"itemId\":7");
        assertThat(json).contains("\"start\":\"2030-01-01T12:00:00\"");
        assertThat(json).contains("\"end\":\"2030-01-02T12:00:00\"");
    }


    // fromJson
    @Test
    void fromJson() throws Exception {
        String json = String.format(
                "{\"itemId\":%d,\"start\":\"%s\",\"end\":\"%s\"}",
                3, "2035-06-01T08:00:00", "2035-06-02T08:00:00");

        BookItemRequestDto dto = mapper.readValue(json, BookItemRequestDto.class);

        assertThat(dto.getItemId()).isEqualTo(3L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2035,6,1,8,0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2035,6,2,8,0));
    }


    // pastStart
    @Test
    void pastStart() {
        BookItemRequestDto dto = new BookItemRequestDto(
                1L,
                NOW.minusDays(1),
                NOW.plusDays(1));

        Set<ConstraintViolation<BookItemRequestDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("start");
    }


    // pastEnd
    @Test
    void pastEnd() {
        BookItemRequestDto dto = new BookItemRequestDto(
                2L,
                NOW.plusHours(2),
                NOW.minusHours(2));

        Set<ConstraintViolation<BookItemRequestDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("end");
    }


    // validDates
    @Test
    void validDates() {
        BookItemRequestDto dto = new BookItemRequestDto(
                5L,
                NOW.plusHours(1),
                NOW.plusHours(5));

        Set<ConstraintViolation<BookItemRequestDto>> v = validator.validate(dto);

        assertThat(v).isEmpty();
    }
}
