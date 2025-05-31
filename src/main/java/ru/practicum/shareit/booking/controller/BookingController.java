package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    public BookingDto create(@RequestBody BookingDto bookingDto,
                             @RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return bookingService.create(bookingDto, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@PathVariable Integer bookingId,
                          @RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllByUser(@RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return bookingService.getAllByUser(userId);
    }
}

