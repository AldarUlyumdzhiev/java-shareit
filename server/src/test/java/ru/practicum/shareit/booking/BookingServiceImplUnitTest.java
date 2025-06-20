package ru.practicum.shareit.booking;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceImplUnitTest {

    @Mock BookingRepository repo;
    @Mock ItemServiceImpl   itemService;
    @Mock UserServiceImpl   userService;
    @InjectMocks BookingServiceImpl service;

    final User  owner  = new User(1L, "Owner",  "own@mail.ru");
    final User  booker = new User(2L, "Booker", "b@mail.ru");
    final Item  item   = Item.builder()
            .id(5L).name("Дрель").description("d")
            .available(true).owner(owner).build();

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    BookingRequestDto makeReq() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(5L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }

    @Test
    @DisplayName("create(): владелец бронирует свою вещь → 404")
    void createOwnerBooksOwnItem() {
        when(itemService.findEntityById(5L)).thenReturn(item);
        BookingRequestDto dto = makeReq();

        assertThatThrownBy(() -> service.create(dto, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("create(): предмет недоступен → 400")
    void createUnavailableItem() {
        Item unavailable = Item.builder().id(6L).available(false).owner(owner).build();
        BookingRequestDto dto = makeReq(); dto.setItemId(6L);

        when(itemService.findEntityById(6L)).thenReturn(unavailable);
        when(userService.findEntityById(2L)).thenReturn(booker);

        assertThatThrownBy(() -> service.create(dto, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("create(): start≥end → 400")
    void create_wrongDates() {
        BookingRequestDto dto = makeReq();
        dto.setEnd(dto.getStart());                            // start == end

        when(itemService.findEntityById(5L)).thenReturn(item);
        when(userService.findEntityById(2L)).thenReturn(booker);

        assertThatThrownBy(() -> service.create(dto, 2L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("create(): happy path — статус WAITING")
    void create_success() {
        when(itemService.findEntityById(5L)).thenReturn(item);
        when(userService.findEntityById(2L)).thenReturn(booker);

        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        when(repo.save(saved.capture())).thenAnswer(i -> i.getArgument(0));

        BookingResponseDto out = service.create(makeReq(), 2L);

        assertThat(out.getStatus()).isEqualTo(Status.WAITING);
        assertThat(saved.getValue().getStatus()).isEqualTo(Status.WAITING);
        assertThat(saved.getValue().getBooker()).isSameAs(booker);
    }

    @Test
    @DisplayName("getById(): чужой пользователь → SecurityException")
    void getByIdForeignUser() {
        Booking stored = Booking.builder().id(9L).booker(booker).item(item)
                .status(Status.WAITING).build();
        when(repo.findById(9L)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.getById(9L, 999L))
                .isInstanceOf(SecurityException.class);
    }

    Booking waiting(Long id) {
        return Booking.builder()
                .id(id)
                .status(Status.WAITING)
                .item(item)
                .booker(booker)
                .build();
    }

    @Test
    @DisplayName("approve(): не владелец → 403")
    void approveNotOwner() {
        when(repo.findById(3L)).thenReturn(Optional.of(waiting(3L)));

        assertThatThrownBy(() -> service.approveBooking(3L, 999L, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("approve(): уже обработано → 400")
    void approveAlreadyProcessed() {
        Booking ready = waiting(4L);
        ready.setStatus(Status.APPROVED);

        when(repo.findById(4L)).thenReturn(Optional.of(ready));

        assertThatThrownBy(() -> service.approveBooking(4L, 1L, true))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("approve(): happy path — отклонение")
    void approveSuccessReject() {
        when(repo.findById(7L)).thenReturn(Optional.of(waiting(7L)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0, Booking.class));

        BookingResponseDto out = service.approveBooking(7L, 1L, false);

        assertThat(out.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    @DisplayName("getAllByOwner(): UNKNOWN state → 400")
    void getAllBadState() {
        when(itemService.findEntitiesByOwner(1L)).thenReturn(List.of(item));

        assertThatThrownBy(() -> service.getAllByOwner(1L, "SOME"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("getAllByOwner(): ALL — возвращает бронирования")
    void getAll() {
        when(itemService.findEntitiesByOwner(1L)).thenReturn(List.of(item));
        when(repo.findByItemIdInOrderByStartDesc(List.of(5L)))
                .thenReturn(List.of(waiting(11L)));

        List<BookingResponseDto> list = service.getAllByOwner(1L, "ALL");

        assertThat(list).hasSize(1)
                .first()
                .extracting(BookingResponseDto::getId)
                .isEqualTo(11L);
    }
}
