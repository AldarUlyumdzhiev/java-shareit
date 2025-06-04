package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long userId);

    ItemWithBookingsDto findById(Long itemId, Long userId);

    List<ItemWithBookingsDto> findAllByOwner(Long userId);

    List<ItemDto> search(String text);

    CommentResponseDto createComment(Long itemId, CommentRequestDto commentRequestDto, Long authorId);
}