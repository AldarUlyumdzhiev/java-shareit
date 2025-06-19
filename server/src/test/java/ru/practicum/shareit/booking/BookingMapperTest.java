package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class BookingMapperTest {

    @Test
    void maptoDtoAndBack() {
        LocalDateTime now = LocalDateTime.now();
        User   booker = User.builder().id(7L).build();
        Item   item   = Item.builder().id(42L).name("Перфоратор").build();

        BookingRequestDto req = new BookingRequestDto();
        req.setItemId(item.getId());
        req.setStart(now.plusDays(1));
        req.setEnd(now.plusDays(2));

        // toBooking
        Booking entity = BookingMapper.toBooking(req, item, booker);
        entity.setId(100L);
        entity.setStatus(Status.APPROVED);

        assertThat(entity.getItem().getId()).isEqualTo(42L);
        assertThat(entity.getBooker().getId()).isEqualTo(7L);

        // toBookingResponseDto
        BookingResponseDto resp = BookingMapper.toBookingResponseDto(entity);

        assertThat(resp.getId()).isEqualTo(100L);
        assertThat(resp.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(resp.getItem().getName()).isEqualTo("Перфоратор");
        assertThat(resp.getBooker().getId()).isEqualTo(7L);

        // toShortDto
        BookingShortDto shortDto = BookingMapper.toShortDto(entity);
        assertThat(shortDto.getId()).isEqualTo(100L);
        assertThat(shortDto.getBookerId()).isEqualTo(7L);
    }
}
