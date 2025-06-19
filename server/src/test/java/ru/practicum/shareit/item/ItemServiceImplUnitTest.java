package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ItemServiceImplUnitTest {

    @Mock ItemRepository        itemRepo;
    @Mock BookingRepository     bookingRepo;
    @Mock UserRepository        userRepo;
    @Mock UserServiceImpl       userService;
    @Mock CommentRepository     commentRepo;
    @Mock ItemRequestRepository requestRepo;

    @InjectMocks ItemServiceImpl service;

    final User owner  = new User(1L, "Owner",  "o@mail.ru");
    final User booker = new User(2L, "Booker", "b@mail.ru");

    @BeforeEach
    void init() { MockitoAnnotations.openMocks(this); }

    @Test
    @DisplayName("create(): blank name -> IllegalArgumentException")
    void createBlankName() {
        ItemDto dto = new ItemDto();
        dto.setName(" ");
        dto.setDescription("d");
        dto.setAvailable(true);

        assertThatThrownBy(() -> service.create(dto, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(itemRepo);
    }

    @Test
    @DisplayName("create(): requestId подтягивает ItemRequest")
    void createWithRequestId() {
        ItemRequest req = ItemRequest.builder().id(9L).description("need").build();
        when(requestRepo.findById(9L)).thenReturn(Optional.of(req));
        when(userService.findEntityById(1L)).thenReturn(owner);

        ArgumentCaptor<Item> saved = ArgumentCaptor.forClass(Item.class);
        when(itemRepo.save(saved.capture())).thenAnswer(i -> i.getArgument(0));

        ItemDto dto = new ItemDto();
        dto.setName("Дрель");
        dto.setDescription("d");
        dto.setAvailable(true);
        dto.setRequestId(9L);

        ItemDto out = service.create(dto, 1L);

        assertThat(out.getRequestId()).isEqualTo(9L);
        assertThat(saved.getValue().getRequest()).isSameAs(req);
    }

    @Test
    @DisplayName("update(): не владелец -> NoSuchElementException")
    void updateNotOwner() {
        Item stored = Item.builder().id(5L).name("x").description("d")
                .available(true).owner(owner).build();
        when(itemRepo.findById(5L)).thenReturn(Optional.of(stored));

        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("y");

        assertThatThrownBy(() -> service.update(5L, patch, 99L))
                .isInstanceOf(NoSuchElementException.class);

        verify(itemRepo, never()).save(any());
    }

    @Test
    @DisplayName("findById(): не владелец — bookings = null")
    void findByIdNotOwner() {
        Item item = Item.builder().id(7L).name("x").description("d")
                .available(true).owner(owner).build();
        when(itemRepo.findById(7L)).thenReturn(Optional.of(item));
        when(commentRepo.findByItemId(7L)).thenReturn(List.of());

        ItemWithBookingsDto dto = service.findById(7L, 777L);

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void searchBlankReturnsEmpty() {
        assertThat(service.search("")).isEmpty();
        assertThat(service.search("   ")).isEmpty();
        verifyNoInteractions(itemRepo);
    }

    @Test
    @DisplayName("update(): userId == null -> выбрасывает IllegalArgumentException")
    void updateNullUserId() {
        Item item = Item.builder()
                .id(10L).name("old").description("desc").available(true).owner(owner)
                .build();

        when(itemRepo.findById(10L)).thenReturn(Optional.of(item));

        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("new");

        assertThatThrownBy(() -> service.update(10L, patch, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be blank");

        verify(itemRepo, never()).save(any());
    }

    @Test
    @DisplayName("findById(): item not found → NoSuchElementException")
    void findByIdItemNotFound() {
        when(itemRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    @DisplayName("update(): изменяет name + description + available")
    void updateSuccess() {
        Item stored = Item.builder()
                .id(15L)
                .name("OldName")
                .description("OldDesc")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepo.findById(15L)).thenReturn(Optional.of(stored));
        when(itemRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("NewName");
        patch.setDescription("NewDesc");
        patch.setAvailable(false);

        ItemDto out = service.update(15L, patch, 1L);

        assertThat(out.getName()).isEqualTo("NewName");
        assertThat(out.getDescription()).isEqualTo("NewDesc");
        assertThat(out.getAvailable()).isFalse();

        verify(itemRepo).save(any());
    }

    @Test
    @DisplayName("findEntityById(): возвращает item по ID")
    void findEntityByIdSuccess() {
        Item expected = Item.builder()
                .id(77L)
                .name("Item77")
                .description("Desc77")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepo.findById(77L)).thenReturn(Optional.of(expected));

        Item result = service.findEntityById(77L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("findAllByOwner(): возвращает список ItemWithBookingsDto c корректными last и next бронированиями")
    void findAllByOwnerSuccess() {
        Item item = Item.builder()
                .id(5L)
                .name("Дрель")
                .description("desc")
                .available(true)
                .owner(owner)
                .build();

        Booking lastBooking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        Booking nextBooking = Booking.builder()
                .id(2L)
                .item(item)
                .booker(booker)
                .status(Status.APPROVED)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(itemRepo.findAllByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingRepo.findByItemIdIn(List.of(5L))).thenReturn(List.of(lastBooking, nextBooking));
        when(commentRepo.findByItemIdIn(List.of(5L))).thenReturn(List.of());

        List<ItemWithBookingsDto> result = service.findAllByOwner(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastBooking().getId()).isEqualTo(1L);
        assertThat(result.get(0).getNextBooking().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("update(): userId = null -> IllegalArgumentException")
    void update_nullUserId() {
        Item item = Item.builder().id(5L).name("x").description("d")
                .available(true).owner(owner).build();

        when(itemRepo.findById(5L)).thenReturn(Optional.of(item));

        ItemUpdateDto patch = new ItemUpdateDto();
        patch.setName("new name");

        assertThatThrownBy(() -> service.update(5L, patch, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("search(): возвращает найденные доступные вещи")
    void search_returnsItems() {
        Item item = Item.builder()
                .id(10L).name("Дрель")
                .description("desc")
                .available(true).owner(owner).build();

        when(itemRepo.searchAvailableItems("дрель"))
                .thenReturn(List.of(item));

        List<ItemDto> results = service.search("дрель");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("findById(): item не найден -> NoSuchElementException")
    void findById_notFound() {
        when(itemRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("findById(): requester = владелец -> возвращает last и next")
    void findById_ownerGetsBookings() {
        Item item = Item.builder()
                .id(7L).name("x").description("d")
                .available(true).owner(owner).build();

        when(itemRepo.findById(7L)).thenReturn(Optional.of(item));
        when(commentRepo.findByItemId(7L)).thenReturn(List.of());
        when(bookingRepo.findLastBooking(eq(7L), eq(Status.APPROVED), any()))
                .thenReturn(Optional.empty());
        when(bookingRepo.findNextBooking(eq(7L), eq(Status.APPROVED), any()))
                .thenReturn(Optional.empty());

        ItemWithBookingsDto dto = service.findById(7L, 1L);

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void searchValidTextReturnsItems() {
        Item item = Item.builder().id(1L).name("дрель").description("др").available(true).owner(owner).build();
        when(itemRepo.searchAvailableItems("др")).thenReturn(List.of(item));

        List<ItemDto> result = service.search("др");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("дрель");
    }


    @Nested
    class CreateComment {

        final Item item = Item.builder()
                .id(3L).name("i").description("d").available(true).owner(owner).build();

        @BeforeEach
        void stubs() {
            when(itemRepo.findById(3L)).thenReturn(Optional.of(item));
            when(userRepo.findById(2L)).thenReturn(Optional.of(booker));
        }

        @Test
        @DisplayName("нет завершённого бронирования -> CommentNotAllowedException")
        void comment_noBooking() {
            when(bookingRepo.hasEndedBooking(eq(2L), eq(3L), eq(Status.APPROVED), any()))
                    .thenReturn(false);

            CommentRequestDto req = new CommentRequestDto();
            req.setText("fail");

            assertThatThrownBy(() -> service.createComment(3L, req, 2L))
                    .isInstanceOf(CommentNotAllowedException.class);

            verifyNoInteractions(commentRepo);
        }

        @Test
        @DisplayName("успешное добавление комментария")
        void comment_success() {
            when(bookingRepo.hasEndedBooking(anyLong(), anyLong(), any(), any()))
                    .thenReturn(true);

            when(commentRepo.save(any())).thenAnswer(i -> {
                Comment c = i.getArgument(0, Comment.class);
                c.setId(55L);
                return c;
            });

            CommentRequestDto req = new CommentRequestDto();
            req.setText("ok");

            CommentResponseDto out = service.createComment(3L, req, 2L);

            assertThat(out.getText()).isEqualTo("ok");
            verify(commentRepo).save(any());
        }
    }
}
