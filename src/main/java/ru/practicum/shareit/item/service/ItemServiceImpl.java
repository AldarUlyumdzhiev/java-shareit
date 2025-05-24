package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final Map<Integer, Item> storage = new HashMap<>();
    private final UserServiceImpl userService;

    @Override
    public ItemCreateDto create(ItemCreateDto itemCreateDto, Integer userId) {
        User owner = userService.findEntityById(userId);
        Item item = ItemMapper.toItem(itemCreateDto);

        int newId = storage.keySet().stream().max(Integer::compareTo).orElse(-1) + 1;

        item.setId(newId);
        item.setOwner(owner);
        storage.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemCreateDto update(Integer itemId, ItemUpdateDto itemDto, Integer userId) {
        if (userId == null) throw new IllegalArgumentException("Missing X-Sharer-User-Id header");

        Item item = storage.get(itemId);
        if (item == null) throw new NoSuchElementException("Item not found");
        if (!item.getOwner().getId().equals(userId)) throw new NoSuchElementException("Only owner can update item");

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemCreateDto findById(Integer itemId) {
        Item item = storage.get(itemId);
        if (item == null) throw new NoSuchElementException("Item not found");
        return ItemMapper.toItemDto(item);
    }

    public Item findEntityById(Integer id) {
        Item item = storage.get(id);
        if (item == null) throw new NoSuchElementException("Item not found");
        return item;
    }

    @Override
    public List<ItemCreateDto> findAllByOwner(Integer userId) {
        return storage.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCreateDto> search(String text) {
        if (text.isBlank()) return Collections.emptyList();
        String lower = text.toLowerCase();
        return storage.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> item.getName().toLowerCase().contains(lower) || item.getDescription().toLowerCase().contains(lower))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
