package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CommentNotAllowedException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User owner = userService.findEntityById(userId);

        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException("Request not found"));
        }

        Item item = ItemMapper.toItem(itemDto, itemRequest);

        if (itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank");
        }
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Item availability must not be blank");
        }

        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, ItemUpdateDto itemDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be blank");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new NoSuchElementException("Only owner can update item");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto findById(Long itemId, Long requesterId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        List<CommentResponseDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());

        BookingShortDto last = null;
        BookingShortDto next = null;
        if (item.getOwner().getId().equals(requesterId)) {
            last = bookingRepository.findLastBooking(
                            itemId, Status.APPROVED, LocalDateTime.now())
                    .map(BookingMapper::toShortDto)
                    .orElse(null);

            next = bookingRepository.findNextBooking(
                            itemId, Status.APPROVED, LocalDateTime.now())
                    .map(BookingMapper::toShortDto)
                    .orElse(null);
        }

        return ItemMapper.toItemWithBookingsDto(item, last, next, comments);
    }



    public Item findEntityById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        return item;
    }

    @Override
    public List<ItemWithBookingsDto> findAllByOwner(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) return List.of();

        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> bookings = bookingRepository.findByItemIdIn(itemIds);
        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<CommentResponseDto>> commentsByItem = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentResponseDto, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), List.of());
                    List<CommentResponseDto> commentDtos = commentsByItem.getOrDefault(item.getId(), List.of());
                    return ItemMapper.toItemWithBookingsDto(
                            item,
                            findLastBooking(itemBookings, now),
                            findNextBooking(itemBookings, now),
                            commentDtos
                    );
                })
                .collect(Collectors.toList());
    }

    private BookingShortDto findLastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isBefore(now) && b.getStatus() == Status.APPROVED)
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toShortDto)
                .orElse(null);
    }

    private BookingShortDto findNextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now) && b.getStatus() == Status.APPROVED)
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toShortDto)
                .orElse(null);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) return List.of();
        return itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponseDto createComment(Long itemId,
                                            CommentRequestDto dto,
                                            Long authorId) {

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Item not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        log.info("=== CREATE COMMENT ATTEMPT ===");
        log.info("Time now: {}", now);
        log.info("Item ID: {}, Author ID: {}", itemId, authorId);
        log.info("Booking must be APPROVED and end_date <= now");

        boolean hadBooking = bookingRepository
                .hasEndedBooking(authorId, itemId, Status.APPROVED, LocalDateTime.now());

        if (!hadBooking) {
            log.warn("""
            userId={} attempted to comment on itemId={}
            Booking not found with: status={}, end_date <= {}
            Booking is not approved or not ended yet.
            """, authorId, itemId, Status.APPROVED, now);

            throw new CommentNotAllowedException(
                    String.format(
                            "Comment is not allowed: user id=%d must have a completed booking for item id=%d with status %s and end date before %s",
                            authorId, itemId, Status.APPROVED, now
                    )
            );
        }

        Comment comment = CommentMapper.toComment(item, dto, author);
        commentRepository.save(comment);

        log.info("Comment saved: \"{}\" by user {} on item {}", comment.getText(), authorId, itemId);

        return CommentMapper.toCommentResponseDto(comment);
    }

    @Override
    public List<Item> findEntitiesByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId);
    }
}
