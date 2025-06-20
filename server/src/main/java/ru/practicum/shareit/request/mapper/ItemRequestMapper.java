package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(CreateItemRequestDto dto, User requestor) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .created(LocalDateTime.now())
                .requestor(requestor)
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemShortDto> itemShorts = items.stream()
                .map(i -> new ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemShorts)
                .build();
    }
}
