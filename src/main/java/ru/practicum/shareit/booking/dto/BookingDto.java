package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Integer id;

    @NotNull(message = "Start date required")
    @Future(message = "Start must be in future")
    private LocalDateTime start;

    @NotNull(message = "End date required")
    @Future(message = "End must be in future")
    private LocalDateTime end;

    @NotNull(message = "Item id required")
    private Integer itemId;

    private Integer bookerId;
    private String status;
}
