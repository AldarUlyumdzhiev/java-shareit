package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ItemRequestMapperTest {

    @Test
    void requestToEntityAndDto() {
        User requestor = User.builder().id(7L).name("Bob").build();

        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель");

        // toItemRequest
        ItemRequest req = ItemRequestMapper.toItemRequest(createDto, requestor);
        assertThat(req.getDescription()).isEqualTo("Нужна дрель");
        assertThat(req.getRequestor().getId()).isEqualTo(7L);
        assertThat(req.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());

        User owner = User.builder().id(10L).name("Alice").build();
        Item item1 = Item.builder().id(100L).name("Перфоратор").owner(owner).build();
        Item item2 = Item.builder().id(101L).name("Отвертка").owner(owner).build();

        req.setId(55L);
        req.setCreated(LocalDateTime.of(2025, 1, 1, 12, 0));

        // toItemRequestDto
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(req, List.of(item1, item2));

        assertThat(dto.getId()).isEqualTo(55L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));

        assertThat(dto.getItems()).hasSize(2)
                .extracting(ItemShortDto::getId, ItemShortDto::getName, ItemShortDto::getOwnerId)
                .containsExactlyInAnyOrder(
                        tuple(100L, "Перфоратор", 10L),
                        tuple(101L, "Отвертка",   10L)
                );
    }
}
