package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserMapperTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void toUserDto_and_back() {
        // entity to dto
        User user = User.builder()
                .id(10L)
                .name("Alice")
                .email("alice@mail.com")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto).extracting(UserDto::getId, UserDto::getName, UserDto::getEmail)
                .containsExactly(10L, "Alice", "alice@mail.com");

        // dto to entity
        dto.setName("Bob");
        dto.setEmail("bob@mail.com");

        User back = UserMapper.toUser(dto);
        assertThat(back.getName()).isEqualTo("Bob");
        assertThat(back.getEmail()).isEqualTo("bob@mail.com");
    }

    @Test
    void validation_blankAndInvalidEmail() {
        UserDto dto = new UserDto();
        dto.setName("");           // blank
        dto.setEmail("wrongMail"); // invalid

        Set<ConstraintViolation<UserDto>> v = validator.validate(dto);

        assertThat(v).hasSize(2);
        assertThat(v)
                .extracting(ConstraintViolation::getMessage)
                .contains("Name must not be blank", "Email must be valid");
    }
}
