package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemWithBookingsDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
    List<CommentResponseDto> comments;
}
