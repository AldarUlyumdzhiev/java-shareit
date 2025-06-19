package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;



@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ItemServiceImplIT {

    @Autowired private ItemService itemService;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private CommentRepository commentRepository;


    // create
    @Test
    @DisplayName("create(): сохраняет ItemDto и проставляет owner")
    void create_item() {
        User owner = userRepository.save(new User(null, "Owner", "own@mail.ru"));

        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("Ударная");
        dto.setAvailable(true);

        ItemDto saved = itemService.create(dto, owner.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Дрель");

        Item inDb = itemRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getOwner().getId()).isEqualTo(owner.getId());
    }


    // update
    @Test
    @DisplayName("update(): владелец может изменить поля через ItemUpdateDto")
    void update_item() {
        User owner = userRepository.save(new User(null, "Owner", "upd@mail.ru"));
        Item item = itemRepository.save(Item.builder()
                .name("Старая").description("desc").available(true).owner(owner).build());

        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("Новая");
        patch.setAvailable(false);

        ItemDto updated = itemService.update(item.getId(), patch, owner.getId());

        assertThat(updated.getName()).isEqualTo("Новая");
        assertThat(updated.getAvailable()).isFalse();
    }


    // findById
    @Test
    @DisplayName("findById(): владельцу возвращает last/next booking")
    void findById_ownerSeeBookings() {
        User owner  = userRepository.save(new User(null, "Owner", "own2@mail.ru"));
        User booker = userRepository.save(new User(null, "Booker", "b@mail.ru"));

        Item item = itemRepository.save(Item.builder()
                .name("Перфоратор").description("desc").available(true).owner(owner).build());

        bookingRepository.save(Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1)).build());

        bookingRepository.save(Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2)).build());

        ItemWithBookingsDto res = itemService.findById(item.getId(), owner.getId());

        assertThat(res.getLastBooking()).isNotNull();
        assertThat(res.getNextBooking()).isNotNull();
    }


    // findAllByOwner
    @Test
    void findAllByOwner_returnsAll() {
        User owner = userRepository.save(new User(null, "O", "o@mail.ru"));
        itemRepository.save(Item.builder().name("A").description("d").available(true).owner(owner).build());
        itemRepository.save(Item.builder().name("B").description("d").available(false).owner(owner).build());

        List<ItemWithBookingsDto> list = itemService.findAllByOwner(owner.getId());
        assertThat(list).hasSize(2);
    }


    // search
    @Test
    void search_returnsOnlyAvailable() {
        User owner = userRepository.save(new User(null, "O2", "o2@mail.ru"));
        itemRepository.save(Item.builder().name("Ключ").description("набор").available(true).owner(owner).build());
        itemRepository.save(
                Item.builder()
                .name("Ключ")
                .description("сломанный")
                .available(false)
                .owner(owner)
                .request(null)
                .build()
        );

        List<ItemDto> found = itemService.search("Клю");
        assertThat(found).hasSize(1).extracting(ItemDto::getAvailable).containsExactly(true);
    }


    // createComment
    @Test
    void createComment_afterApprovedBooking() {
        User owner  = userRepository.save(new User(null, "O3", "o3@mail.ru"));
        User booker = userRepository.save(new User(null, "B3", "b3@mail.ru"));
        Item item   = itemRepository.save(
                Item.builder()
                .name("Лобзик")
                .description("d")
                .available(true)
                .owner(owner)
                .build()
        );

        bookingRepository.save(Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1)).build());

        CommentRequestDto req = new CommentRequestDto();
        req.setText("Отличный инструмент");

        CommentResponseDto resp = itemService.createComment(item.getId(), req, booker.getId());

        assertThat(resp.getText()).isEqualTo("Отличный инструмент");
        assertThat(commentRepository.count()).isEqualTo(1);
    }
}
