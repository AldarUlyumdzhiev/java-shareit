package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JsonTest
class ItemDtoJsonTest {

    @Autowired private ObjectMapper mapper;
    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();


    // toJson
    @Test
    void toJson() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(9L);
        dto.setName("Ключ");
        dto.setDescription("Набор");
        dto.setAvailable(true);
        dto.setRequestId(4L);

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":9");
        assertThat(json).contains("\"name\":\"Ключ\"");
        assertThat(json).contains("\"description\":\"Набор\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":4");
    }


    // blankFields
    @Test
    void blankFields() {
        ItemDto dto = new ItemDto(); // все обязательные поля пусты

        Set<ConstraintViolation<ItemDto>> v = validator.validate(dto);

        assertThat(v).hasSize(3); // name, description, available
    }


    // commBlank
    @Test
    void commBlank() {
        CommentRequestDto dto = new CommentRequestDto(); // text null

        Set<ConstraintViolation<CommentRequestDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .asString()
                .isEqualTo("text");
    }


    // commOk
    @Test
    void commOk() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Отлично!");

        Set<ConstraintViolation<CommentRequestDto>> v = validator.validate(dto);

        assertThat(v).isEmpty();
    }
}
