package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDtoWithBookings() {
        User owner = User.builder().id(7L).name("Owner").build();
        ItemRequest request = ItemRequest.builder().id(55L).description("Нужен дрель").requestor(owner).build();

        Item item = Item.builder()
                .id(42L)
                .name("Перфоратор")
                .description("Ударный")
                .available(true)
                .request(request)
                .owner(owner)
                .build();

        // toItemDto
        ItemDto dto = ItemMapper.toItemDto(item);
        assertThat(dto).extracting(ItemDto::getId, ItemDto::getRequestId, ItemDto::getAvailable)
                .containsExactly(42L, 55L, true);

        // toItem
        Item back = ItemMapper.toItem(dto, request);
        assertThat(back.getName()).isEqualTo("Перфоратор");
        assertThat(back.getRequest().getId()).isEqualTo(55L);

        // toItemWithBookingsDto
        BookingShortDto last = new BookingShortDto();
        last.setId(1L); last.setBookerId(10L);
        BookingShortDto next = new BookingShortDto();
        next.setId(2L); next.setBookerId(11L);

        CommentResponseDto c1 = new CommentResponseDto();
        c1.setId(100L); c1.setText("Отличная вещь"); c1.setAuthorName("Bob");
        ItemWithBookingsDto w = ItemMapper.toItemWithBookingsDto(item, last, next, List.of(c1));

        assertThat(w.getComments()).hasSize(1);
        assertThat(w.getLastBooking().getId()).isEqualTo(1L);
        assertThat(w.getNextBooking().getBookerId()).isEqualTo(11L);
    }
}
