package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String SHARER_ID_HEADER = "X-Sharer-User-Id";
    private static final String ITEM_ID_PATH = "/{itemId}";

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(SHARER_ID_HEADER) long userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping(ITEM_ID_PATH)
    public ResponseEntity<Object> updateItem(@RequestHeader(SHARER_ID_HEADER) long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid ItemUpdateDto itemDto
                                             ) {
        if (itemDto.getName() == null && itemDto.getDescription() == null && itemDto.getAvailable() == null) {
            return ResponseEntity.badRequest().body("At least one field must be changed to update.");
        }
        log.info("Updating item {}, itemId={}, userId={}", itemDto, itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping(ITEM_ID_PATH)
    public ResponseEntity<Object> getItem(@RequestHeader(SHARER_ID_HEADER) long userId,
                                          @PathVariable Long itemId) {
        log.info("Getting item with id={}, userId={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }






    @PostMapping(ITEM_ID_PATH + "/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(SHARER_ID_HEADER) long authorId,
                                                @PathVariable Long itemId,
                                                @RequestBody @Valid CommentRequestDto commentRequestDto) {
        log.info("Creating comment for itemId={}, authorId={}, comment={}", itemId, authorId, commentRequestDto);
        return itemClient.createComment(authorId, itemId, commentRequestDto);
    }








    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(SHARER_ID_HEADER) long userId) {
        log.info("Getting all items for userId={}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text) {
        log.info("Searching items with text='{}'", text);
        return itemClient.search(text);
    }
}
