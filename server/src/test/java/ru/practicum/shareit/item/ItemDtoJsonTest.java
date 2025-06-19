package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
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
    @Autowired private Validator validator;

    // serialize
    @Test
    void itemDtoToJson() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(12L);
        dto.setName("Перфоратор");
        dto.setDescription("Ударный");
        dto.setAvailable(true);
        dto.setRequestId(5L);

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":12");
        assertThat(json).contains("\"name\":\"Перфоратор\"");
        assertThat(json).contains("\"description\":\"Ударный\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":5");
    }


    // blank fields validation
    // name null, description null, available null
    @Test
    void itemValidateBlankFields() {
        ItemDto dto = new ItemDto();

        Set<ConstraintViolation<ItemDto>> v = validator.validate(dto);

        assertThat(v).hasSize(3);
        assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("name"));
        assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("description"));
        assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("available"));
    }


    // blank text validation
    @Test
    void commentValidateBlankText() {
        CommentRequestDto dto = new CommentRequestDto();

        Set<ConstraintViolation<CommentRequestDto>> v = validator.validate(dto);

        assertThat(v).hasSize(1)
                .first()
                .extracting(ConstraintViolation::getMessage)
                .asString()
                .contains("must not be blank");
    }


    // text validation
    @Test
    void commentValidateText() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Хорошая вещь");

        Set<ConstraintViolation<CommentRequestDto>> v = validator.validate(dto);

        assertThat(v).isEmpty();
    }
}
