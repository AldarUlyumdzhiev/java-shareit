package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDto {
    @NotNull(message = "Item id is required")
    Long itemId;

    @NotNull @Future
    LocalDateTime start;

    @NotNull @Future
    LocalDateTime end;
}
