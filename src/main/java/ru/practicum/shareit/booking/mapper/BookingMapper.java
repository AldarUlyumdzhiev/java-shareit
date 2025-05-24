package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItemId(booking.getItem() != null ? booking.getItem().getId() : null);
        dto.setBookerId(booking.getBooker() != null ? booking.getBooker().getId() : null);
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        return dto;
    }

    public static Booking toEntity(BookingDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(dto.getStatus() != null ? Status.valueOf(dto.getStatus()) : null);
        return booking;
    }
}
