package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    Integer id;

    @NotNull(message = "Start date required")
    @Future(message = "Start must be in future")
    LocalDateTime start;

    @NotNull(message = "End date required")
    @Future(message = "End must be in future")
    LocalDateTime end;

    @NotNull(message = "Item id required")
    Integer itemId;

    Integer bookerId;
    String status;
}
