package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto dto, Integer userId);
    List<ItemRequestDto> getRequestsByUser(Integer userId);
    ItemRequestDto getById(Integer requestId, Integer userId);
    List<ItemRequestDto> getAll(Integer userId);
}
