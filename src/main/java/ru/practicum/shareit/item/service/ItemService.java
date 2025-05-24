package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemCreateDto create(ItemCreateDto itemCreateDto, Integer userId);
    ItemCreateDto update(Integer itemId, ItemUpdateDto itemUpdateDto, Integer userId);
    ItemCreateDto findById(Integer itemId);
    List<ItemCreateDto> findAllByOwner(Integer userId);
    List<ItemCreateDto> search(String text);
}