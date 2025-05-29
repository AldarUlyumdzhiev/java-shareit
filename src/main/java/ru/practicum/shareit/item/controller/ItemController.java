package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import static ru.practicum.shareit.util.Constants.SHARER_ID_HEADER;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemCreateDto create(@Valid @RequestBody ItemCreateDto itemCreateDto,
                                @RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return itemService.create(itemCreateDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemCreateDto update(@PathVariable Integer itemId,
                                @RequestBody ItemUpdateDto itemDto,
                                @RequestHeader(value = SHARER_ID_HEADER, required = false) Integer userId) {
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemCreateDto getById(@PathVariable Integer itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemCreateDto> getAllByOwner(@RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemCreateDto> search(@RequestParam String text) {
        return itemService.search(text);
    }
}
