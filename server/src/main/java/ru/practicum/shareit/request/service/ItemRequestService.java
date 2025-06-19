package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(CreateItemRequestDto dto, Long userId);

    List<ItemRequestDto> getAllRequestsByUser(Long userId);

    ItemRequestDto getById(Long itemRequestId, Long userId);

    List<ItemRequestDto> getAllUserRequests(Long userId, int from, int size);
}
