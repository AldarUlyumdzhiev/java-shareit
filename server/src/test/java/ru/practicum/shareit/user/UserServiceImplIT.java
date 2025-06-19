package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class UserServiceImplIT {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;


    // create
    @Test
    @DisplayName("create(): сохраняет юзера и проставляет id")
    void create_user() {
        UserDto dto = new UserDto();
        dto.setName("Bob");
        dto.setEmail("bob@mail.ru");

        UserDto saved = userService.create(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("bob@mail.ru");

        User fromDb = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("create(): повторный email -> IllegalStateException")
    void create_duplicateEmail() {
        userRepository.save(User.builder().name("Bob").email("dup@mail.ru").build());

        UserDto dup = new UserDto();
        dup.setName("Rob");
        dup.setEmail("dup@mail.ru");

        assertThatThrownBy(() -> userService.create(dup))
                .isInstanceOf(IllegalStateException.class);
    }


    // update
    @Test
    @DisplayName("update(): можно изменить имя и email")
    void update_user() {
        User origin = userRepository.save(
                User.builder().name("Old").email("old@mail.ru").build());

        UserDto patch = new UserDto();
        patch.setName("NewName");
        patch.setEmail("new@mail.ru");

        UserDto after = userService.update(origin.getId(), patch);

        assertThat(after.getName()).isEqualTo("NewName");
        assertThat(after.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    @DisplayName("update(): email занят -> IllegalStateException")
    void update_duplicateEmail() {
        userRepository.save(User.builder().name("One").email("one@mail.ru").build());
        User u2 = userRepository.save(
                User.builder().name("Two").email("two@mail.ru").build());

        UserDto patch = new UserDto();
        patch.setEmail("one@mail.ru");

        assertThatThrownBy(() -> userService.update(u2.getId(), patch))
                .isInstanceOf(IllegalStateException.class);
    }


    // findById
    @Test
    void findById_returnsDto() {
        User saved = userRepository.save(
                User.builder().name("FindMe").email("find@mail.ru").build());

        UserDto dto = userService.findById(saved.getId());

        assertThat(dto.getName()).isEqualTo("FindMe");
    }


    // findAll
    @Test
    void findAll_returnsAll() {
        userRepository.save(User.builder().name("U1").email("u1@mail.ru").build());
        userRepository.save(User.builder().name("U2").email("u2@mail.ru").build());

        List<UserDto> all = userService.findAll();

        assertThat(all).hasSize(2)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("u1@mail.ru", "u2@mail.ru");
    }
}
