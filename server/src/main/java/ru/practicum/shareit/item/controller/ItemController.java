package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.util.Constants.ITEM_ID_PATH;
import static ru.practicum.shareit.util.Constants.SHARER_ID_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestBody ItemDto itemDto,
                          @RequestHeader(SHARER_ID_HEADER) Long userId) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping(ITEM_ID_PATH)
    public ItemDto update(@PathVariable Long itemId,
                                @RequestBody ItemUpdateDto itemDto,
                                @RequestHeader(value = SHARER_ID_HEADER, required = false) Long userId) {
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping(ITEM_ID_PATH)
    public ItemWithBookingsDto getById(
            @RequestHeader(SHARER_ID_HEADER) Long userId,
            @PathVariable Long itemId
    ) {
        return itemService.findById(itemId, userId);
    }

    @PostMapping(ITEM_ID_PATH + "/comment")
    public CommentResponseDto createComment(@PathVariable Long itemId,
                                            @RequestBody CommentRequestDto commentRequestDto,
                                            @RequestHeader(SHARER_ID_HEADER) Long authorId) {
        return itemService.createComment(itemId, commentRequestDto, authorId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getAllByOwner(@RequestHeader(SHARER_ID_HEADER) Long userId) {
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }
}
