package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class RequestDtoJsonTest {

    @Autowired private ObjectMapper mapper;
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();


    // toJson
    @Test
    void toJson() throws Exception {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Перфоратор");

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"description\":\"Перфоратор\"");
    }


    // blank
    // description == null
    @Test
    void blank() {
        CreateItemRequestDto dto = new CreateItemRequestDto();

        Set<ConstraintViolation<CreateItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("description");
    }


    // ok
    @Test
    void ok() {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Газонокосилка");

        Set<ConstraintViolation<CreateItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
