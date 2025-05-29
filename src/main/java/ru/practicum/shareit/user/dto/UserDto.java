package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Integer id;

    @NotBlank(message = "Name must not be blank")
    String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    String email;
}
