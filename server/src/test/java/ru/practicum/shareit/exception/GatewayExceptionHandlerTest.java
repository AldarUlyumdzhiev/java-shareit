package ru.practicum.shareit.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayExceptionHandlerTest {

    @Test
    @DisplayName("badRequest: возвращает 400 с сообщением")
    void badRequest() {
        GatewayExceptionHandler handler = new GatewayExceptionHandler();
        Map<String, String> response = handler.badRequest(new IllegalArgumentException("bad request"));

        assertThat(response).containsEntry("error", "bad request");
    }
}
