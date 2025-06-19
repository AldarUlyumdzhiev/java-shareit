package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class UserDtoJsonTest {

    @Autowired private ObjectMapper mapper;
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();


    // toJson
    @Test
    void toJson() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Bob");
        dto.setEmail("bob@mail.ru");

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Bob\"");
        assertThat(json).contains("\"email\":\"bob@mail.ru\"");
    }


    // fromJson
    @Test
    void fromJson() throws Exception {
        String json = "{\"id\":2,\"name\":\"Bob\",\"email\":\"bob@mail.ru\"}";

        UserDto dto = mapper.readValue(json, UserDto.class);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getEmail()).isEqualTo("bob@mail.ru");
    }


    // badEmail
    @Test
    void badEmail() {
        UserDto dto = new UserDto();
        dto.setName("Ald");
        dto.setEmail("wrong");

        Set<ConstraintViolation<UserDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("email");
    }


    // blankName
    @Test
    void blankName() {
        UserDto dto = new UserDto();
        dto.setName("");
        dto.setEmail("ok@mail.ru");

        Set<ConstraintViolation<UserDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("name");
    }
}
