package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    @Override
    public BookingResponseDto create(BookingRequestDto dto, Long userId) {
        User booker = userService.findEntityById(userId);
        Item item   = itemService.findEntityById(dto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner can't book own item");
        }
        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is unavailable");
        }
        if (dto.getStart().isAfter(dto.getEnd()) || dto.getStart().equals(dto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start must be before end");
        }
        if (dto.getStart().isBefore(LocalDateTime.now()) || dto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking dates must be in future");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByUser(Long userId) {
        return bookingRepository.findByBookerId(userId).stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        log.info("{} request for booking ID={} by ownerId={}",
                approved ? "Approval" : "Rejection", bookingId, ownerId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking with ID={} not found", bookingId);
                    return new NoSuchElementException("Booking not found");
                });

        Long bookingOwnerId = booking.getItem().getOwner().getId();
        if (!bookingOwnerId.equals(ownerId)) {
            log.warn("User {} is not the owner of the item. Expected ownerId={}", ownerId, bookingOwnerId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can approve bookings");
        }

        if (booking.getStatus() != Status.WAITING) {
            log.warn("Booking already processed: status = {}", booking.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking already processed");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);

        log.info("Booking ID={} status updated to {} at {}", bookingId, booking.getStatus(), LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        log.info("Booking saved: ID={}, status={}", saved.getId(), saved.getStatus());

        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String stateParam) {
        List<Item> items = itemService.findEntitiesByOwner(ownerId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no items.");
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> bookings;
        Status status;
        try {
            status = Status.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (!stateParam.equalsIgnoreCase("ALL")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown booking state: " + stateParam);
            }
            status = null; // Для ALL — статус не используется
        }

        if (status == null) {
            bookings = bookingRepository.findByItemIdInOrderByStartDesc(itemIds);
        } else {
            bookings = bookingRepository.findByItemIdInAndStatusOrderByStartDesc(itemIds, status);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}
