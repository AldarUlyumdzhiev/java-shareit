package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto  create(BookingRequestDto  bookingDto, Long userId);

    BookingResponseDto  getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByUser(Long userId);

    BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved);

    List<BookingResponseDto> getAllByOwner(Long ownerId, String state);
}
