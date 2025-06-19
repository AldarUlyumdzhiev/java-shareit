package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    @DisplayName("create(): blank name → IllegalArgumentException")
    void create_blankName() {
        ItemDto dto = new ItemDto();               // <— без all-args конструктора
        dto.setName(" ");
        dto.setDescription("d");
        dto.setAvailable(true);

        assertThatThrownBy(() -> service.create(dto, 1L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(itemRepo);
    }

    @Test
    @DisplayName("create(): requestId подтягивает ItemRequest")
    void create_withRequestId() {
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
    @DisplayName("update(): не-владелец → NoSuchElementException")
    void update_notOwner() {
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
    void findById_notOwner() {
        Item item = Item.builder().id(7L).name("x").description("d")
                .available(true).owner(owner).build();
        when(itemRepo.findById(7L)).thenReturn(Optional.of(item));
        when(commentRepo.findByItemId(7L)).thenReturn(List.of());

        ItemWithBookingsDto dto = service.findById(7L, 777L);

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void search_blankReturnsEmpty() {
        assertThat(service.search("")).isEmpty();
        assertThat(service.search("   ")).isEmpty();
        verifyNoInteractions(itemRepo);
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
        @DisplayName("нет завершённого бронирования → CommentNotAllowedException")
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
                c.setId(55L);                                // заполним id, чтобы сериализация не упала
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
