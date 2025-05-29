package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDto {
    Integer id;

    @NotBlank(message = "Description required")
    String description;

    LocalDateTime created;
}