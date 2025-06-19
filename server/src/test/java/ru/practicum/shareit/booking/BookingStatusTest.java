package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Status;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {

    @Test
    void shouldReturnCorrectEnum() {
        assertEquals(Status.WAITING, Status.valueOf("WAITING"));
        assertEquals(Status.APPROVED, Status.valueOf("APPROVED"));
        assertEquals(Status.REJECTED, Status.valueOf("REJECTED"));
    }

    @Test
    void shouldThrowExceptionForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> Status.valueOf("UNKNOWN"));
    }

    @Test
    void shouldContainAllStatuses() {
        Status[] statuses = Status.values();
        assertEquals(3, statuses.length);
        assertArrayEquals(new Status[]{Status.WAITING, Status.APPROVED, Status.REJECTED}, statuses);
    }
}
