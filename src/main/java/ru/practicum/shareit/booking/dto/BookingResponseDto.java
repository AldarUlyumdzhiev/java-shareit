package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponseDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    Status status;

    ItemInfo item;
    BookerInfo booker;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ItemInfo {
        Long id;
        String name;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class BookerInfo {
        Long id;
    }
}
