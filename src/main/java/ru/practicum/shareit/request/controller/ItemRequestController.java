package ru.practicum.shareit.request.controller;

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
    public ItemRequestDto create(@RequestBody ItemRequestDto requestDto,
                                 @RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return requestService.create(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return requestService.getRequestsByUser(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Integer requestId,
                                  @RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return requestService.getById(requestId, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(SHARER_ID_HEADER) Integer userId) {
        return requestService.getAll(userId);
    }
}
