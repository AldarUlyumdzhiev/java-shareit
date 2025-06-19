package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static Booking toBooking(BookingRequestDto dto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        BookingResponseDto.ItemInfo itemInfo = new BookingResponseDto.ItemInfo();
        itemInfo.setId(booking.getItem().getId());
        itemInfo.setName(booking.getItem().getName());
        dto.setItem(itemInfo);

        BookingResponseDto.BookerInfo bookerInfo = new BookingResponseDto.BookerInfo();
        bookerInfo.setId(booking.getBooker().getId());
        dto.setBooker(bookerInfo);

        return dto;
    }

    public static BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(
                booking.getBooker() != null ? booking.getBooker().getId() : null
        );
        return dto;
    }
}
