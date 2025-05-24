package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/requests")
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestBody ItemRequestDto requestDto,
                                 @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.create(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.getRequestsByUser(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Integer requestId,
                                  @RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.getById(requestId, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        return requestService.getAll(userId);
    }
}
