package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemCreateDto create(@Valid @RequestBody ItemCreateDto itemCreateDto,
                                @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.create(itemCreateDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemCreateDto update(@PathVariable Integer itemId,
                                @RequestBody ItemUpdateDto itemDto,
                                @RequestHeader(value = "X-Sharer-User-Id", required = false) Integer userId) {
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemCreateDto getById(@PathVariable Integer itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemCreateDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemCreateDto> search(@RequestParam String text) {
        return itemService.search(text);
    }
}
