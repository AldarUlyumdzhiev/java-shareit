package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto bookingDto, Integer userId);
    BookingDto getById(Integer bookingId, Integer userId);
    List<BookingDto> getAllByUser(Integer userId);
}
