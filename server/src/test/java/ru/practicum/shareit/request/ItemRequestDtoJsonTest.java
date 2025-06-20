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
class ItemRequestDtoJsonTest {

    @Autowired private ObjectMapper mapper;
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    // serialize
    @Test
    void descToJson() throws Exception {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Нужно ведро");

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"description\":\"Нужно ведро\"");
    }

    // deserialize
    @Test
    void readsDescFromJson() throws Exception {
        String json = "{\"description\":\"Отбойный молоток\"}";

        CreateItemRequestDto dto = mapper.readValue(json, CreateItemRequestDto.class);

        assertThat(dto.getDescription()).isEqualTo("Отбойный молоток");
    }

    // validation
    @Test
    void validationBlankDescription() {
        CreateItemRequestDto dto = new CreateItemRequestDto(); // description == null

        Set<ConstraintViolation<CreateItemRequestDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getMessage)
                .asString()
                .contains("required");
    }

    @Test
    void validationValidDescription() {
        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Газонокосилка");

        Set<ConstraintViolation<CreateItemRequestDto>> v = validator.validate(dto);

        assertThat(v).isEmpty();
    }
}
