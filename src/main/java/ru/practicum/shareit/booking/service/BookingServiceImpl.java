package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final Map<Integer, Booking> storage = new HashMap<>();
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;

    @Override
    public BookingDto create(BookingDto bookingDto, Integer userId) {
        User booker = userService.findEntityById(userId);
        Item item = itemService.findEntityById(bookingDto.getItemId());

        Booking booking = new Booking();
        int newId = storage.keySet().stream().max(Integer::compareTo).orElse(-1) + 1;

        booking.setId(newId);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(Status.WAITING);

        storage.put(booking.getId(), booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getById(Integer bookingId, Integer userId) {
        Booking booking = storage.get(bookingId);
        if (booking == null) throw new NoSuchElementException("Booking not found");

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByUser(Integer userId) {
        return storage.values().stream()
                .filter(b -> b.getBooker().getId().equals(userId))
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }
}
