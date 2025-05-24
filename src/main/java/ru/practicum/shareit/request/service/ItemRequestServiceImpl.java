package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final Map<Integer, ItemRequest> storage = new HashMap<>();
    private final UserServiceImpl userService;

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Integer userId) {
        User user = userService.findEntityById(userId);
        ItemRequest request = ItemRequestMapper.toEntity(dto);

        int newId = storage.keySet().stream().max(Integer::compareTo).orElse(-1) + 1;

        request.setId(newId);
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now());
        storage.put(request.getId(), request);
        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> getRequestsByUser(Integer userId) {
        return storage.values().stream()
                .filter(r -> r.getRequestor().getId().equals(userId))
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Integer requestId, Integer userId) {
        if (!storage.containsKey(requestId)) {
            throw new NoSuchElementException("Request not found");
        }
        return ItemRequestMapper.toDto(storage.get(requestId));
    }

    @Override
    public List<ItemRequestDto> getAll(Integer userId) {
        return storage.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(userId))
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }
}
