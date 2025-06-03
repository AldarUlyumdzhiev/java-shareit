package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.util.Constants.SHARER_ID_HEADER;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader(SHARER_ID_HEADER) Long userId) {
        return requestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader(SHARER_ID_HEADER) Long userId) {
        return requestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Long itemRequestId,
                                  @RequestHeader(SHARER_ID_HEADER) Long userId) {
        return requestService.getById(itemRequestId, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(SHARER_ID_HEADER) Long userId) {
        return requestService.getAllUserRequests(userId);
    }
}
