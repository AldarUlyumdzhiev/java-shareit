package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private final BookingClient bookingClient;
	private static final String SHARER_ID_HEADER = "X-Sharer-User-Id";

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(SHARER_ID_HEADER) long userId,
										 @RequestBody @Valid BookItemRequestDto dto) {
		return bookingClient.create(userId, dto);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> get(@PathVariable long id,
									  @RequestHeader(SHARER_ID_HEADER) long userId) {
		return bookingClient.get(id, userId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllByUser(
			@RequestHeader(SHARER_ID_HEADER) Long userId,
			@RequestParam(defaultValue = "ALL") String state
	) {
		BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown state: %s", state)));
		log.info("Getting bookings for user={}, state={}", userId, bookingState);

		return bookingClient.getBookings(userId);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Object> approve(@PathVariable long id,
										  @RequestParam boolean approved,
										  @RequestHeader(SHARER_ID_HEADER) long ownerId) {
		return bookingClient.approve(id, ownerId, approved);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllByOwner(@RequestHeader(SHARER_ID_HEADER) long ownerId,
												@RequestParam(defaultValue = "ALL") String state) {
		BookingState bookingState = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Unknown state: %s", state)));
		return bookingClient.getAllByOwner(ownerId, bookingState);
	}
}
