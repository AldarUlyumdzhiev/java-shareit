package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class BookingServiceImplIT {

    @Autowired private BookingService bookingService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UserRepository userRepository;

    // create
    @Test
    @DisplayName("create(): booker может создать 'WAITING' бронь на чужой item")
    void createBookingSuccess() {
        User owner  = userRepository.save(User.builder().name("Owner").email("o@mail.ru").build());
        User booker = userRepository.save(User.builder().name("Booker").email("b@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Дрель").description("desc").available(true).owner(owner).build());

        BookingRequestDto req = new BookingRequestDto();
        req.setItemId(item.getId());
        req.setStart(LocalDateTime.now().plusDays(1));
        req.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto resp = bookingService.create(req, booker.getId());

        assertThat(resp.getStatus()).isEqualTo(Status.WAITING);
        assertThat(bookingRepository.count()).isEqualTo(1);
    }

    // approveBooking
    @Test
    @DisplayName("approveBooking(): владелец переводит WAITING в APPROVED")
    void approveBooking() {
        User owner  = userRepository.save(User.builder().name("O").email("o2@mail.ru").build());
        User booker = userRepository.save(User.builder().name("B").email("b2@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Лобзик").description("d").available(true).owner(owner).build());

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .item(item).booker(booker).status(Status.WAITING)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build());

        BookingResponseDto resp = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertThat(resp.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    @DisplayName("approveBooking(): не-владелец → FORBIDDEN")
    void approveBookingNotOwner() {
        User owner  = userRepository.save(User.builder().name("O3").email("o3@mail.ru").build());
        User booker = userRepository.save(User.builder().name("B3").email("b3@mail.ru").build());
        User stranger = userRepository.save(User.builder().name("X").email("x@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Пила").description("d").available(true).owner(owner).build());

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .item(item).booker(booker).status(Status.WAITING)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build());

        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), stranger.getId(), true))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    // getById
    @Test
    @DisplayName("getById(): доступен владельцу и бронирующему")
    void getByIdOwnerAndBooker() {
        User owner  = userRepository.save(User.builder().name("O4").email("o4@mail.ru").build());
        User booker = userRepository.save(User.builder().name("B4").email("b4@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Фрезер").description("d").available(true).owner(owner).build());

        Booking booking = bookingRepository.save(
                Booking.builder()
                        .item(item).booker(booker).status(Status.WAITING)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build());

        // для booker
        BookingResponseDto byBooker = bookingService.getById(booking.getId(), booker.getId());
        assertThat(byBooker.getId()).isEqualTo(booking.getId());

        // для owner
        BookingResponseDto byOwner = bookingService.getById(booking.getId(), owner.getId());
        assertThat(byOwner.getId()).isEqualTo(booking.getId());
    }

    // getAllByUser
    @Test
    void getAllByUserReturnsList() {
        User owner  = userRepository.save(User.builder().name("O5").email("o5@mail.ru").build());
        User booker = userRepository.save(User.builder().name("B5").email("b5@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Молоток").description("d").available(true).owner(owner).build());

        bookingRepository.save(
                Booking.builder()
                        .item(item).booker(booker).status(Status.APPROVED)
                        .start(LocalDateTime.now().plusDays(1))
                        .end(LocalDateTime.now().plusDays(2))
                        .build());

        List<BookingResponseDto> list = bookingService.getAllByUser(booker.getId());

        assertThat(list).hasSize(1)
                .extracting(BookingResponseDto::getStatus)
                .containsExactly(Status.APPROVED);
    }

    // getAllByOwner
    @Test
    void getAllByOwnerAllState() {
        User owner  = userRepository.save(User.builder().name("O6").email("o6@mail.ru").build());
        User booker = userRepository.save(User.builder().name("B6").email("b6@mail.ru").build());

        Item item = itemRepository.save(
                Item.builder().name("Отвертка").description("d").available(true).owner(owner).build());

        bookingRepository.save(
                Booking.builder()
                        .item(item).booker(booker).status(Status.REJECTED)
                        .start(LocalDateTime.now().plusDays(2))
                        .end(LocalDateTime.now().plusDays(3))
                        .build());

        List<BookingResponseDto> all = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(all).hasSize(1)
                .extracting(BookingResponseDto::getStatus)
                .containsExactly(Status.REJECTED);
    }
}
