package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;
    private static final String SHARER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(SHARER_ID_HEADER) Long userId,
                                                @RequestBody @Valid CreateItemRequestDto requestDto) {
        return requestClient.createRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(SHARER_ID_HEADER) Long userId) {
        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(SHARER_ID_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "10") @Positive Integer size) {
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(SHARER_ID_HEADER) Long userId,
                                                 @PathVariable Long requestId) {
        return requestClient.getRequestById(userId, requestId);
    }
}
