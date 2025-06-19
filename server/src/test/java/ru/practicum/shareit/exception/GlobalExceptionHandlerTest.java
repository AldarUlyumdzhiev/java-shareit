package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFoundException: возвращает 404 с сообщением")
    void handleNotFoundException() {
        var ex = new NoSuchElementException("Not found");
        var response = handler.handleNotFoundException(ex);

        assertThat(response).containsEntry("error", "Not found");
    }

    @Test
    @DisplayName("handleValidationException: возвращает 400 с сообщением")
    void handleValidationException() {
        var ex = new IllegalArgumentException("Invalid data");
        var response = handler.handleValidationException(ex);

        assertThat(response).containsEntry("error", "Invalid data");
    }

    @Test
    @DisplayName("handleConstraintViolation: возвращает 400 с сообщением")
    void handleConstraintViolation() {
        var ex = new ConstraintViolationException("Constraint failed", null);
        var response = handler.handleConstraintViolation(ex);

        assertThat(response).containsEntry("error", "Constraint failed");
    }

    @Test
    @DisplayName("handleSecurity: возвращает 500 с сообщением")
    void handleSecurity() {
        var ex = new SecurityException("Access denied");
        var response = handler.handleSecurity(ex);

        assertThat(response).containsEntry("error", "Access denied");
    }

    @Test
    @DisplayName("handleGeneralException: возвращает 500 и details")
    void handleGeneralException() {
        var ex = new Exception("Some server error");
        var response = handler.handleGeneralException(ex);

        assertThat(response)
                .containsEntry("error", "Внутренняя ошибка сервера")
                .containsEntry("details", "Some server error");
    }

    @Test
    @DisplayName("handleCommentNotAllowed: возвращает 400 с сообщением")
    void handleCommentNotAllowed() {
        var ex = new CommentNotAllowedException("Comment rejected");
        var response = handler.handleCommentNotAllowed(ex);

        assertThat(response).containsEntry("error", "Comment rejected");
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid: извлекает первую ошибку")
    void handleMethodArgumentNotValid() {
        ObjectError error = new ObjectError("field", "Invalid input");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        var response = handler.handleMethodArgumentNotValid(ex);

        assertThat(response).containsEntry("error", "Invalid input");
    }
}
