package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.util.Constants.SHARER_ID_HEADER;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestBody @Valid BookingRequestDto bookingDto,
                                         @RequestHeader(SHARER_ID_HEADER) Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto get(@PathVariable Long bookingId,
                          @RequestHeader(SHARER_ID_HEADER) Long userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByUser(@RequestHeader(SHARER_ID_HEADER) Long userId) {
        return bookingService.getAllByUser(userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@PathVariable Long bookingId,
                                      @RequestParam("approved") boolean approved,
                                      @RequestHeader(SHARER_ID_HEADER) Long ownerId) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }
}

