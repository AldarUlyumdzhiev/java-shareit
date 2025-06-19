package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;
    private static final String USER_ID_PATH = "/{id}";

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        log.info("Creating user={}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping(USER_ID_PATH)
    public ResponseEntity<Object> updateUser(@PathVariable Long id,
                                             @RequestBody UserDto userDto) {
        log.info("Updating user with id={}, user={}", id, userDto);
        return userClient.updateUser(id, userDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Getting all users");
        return userClient.getUsers();
    }

    @GetMapping(USER_ID_PATH)
    public ResponseEntity<Object> getUser(@PathVariable Long id) {
        log.info("Getting user with id={}", id);
        return userClient.getUser(id);
    }

    @DeleteMapping(USER_ID_PATH)
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("Deleting user with id={}", id);
        return userClient.deleteUser(id);
    }
}
