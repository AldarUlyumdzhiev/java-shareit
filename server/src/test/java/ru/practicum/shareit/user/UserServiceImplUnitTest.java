package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class UserServiceImplUnitTest {

    @Mock            UserRepository repo;
    @InjectMocks     UserServiceImpl service;

    @BeforeEach void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("create(): happy-path сохраняет пользователя")
    void create() {
        UserDto dto = new UserDto();
        dto.setName("Vasya");
        dto.setEmail("v@mail.ru");

        when(repo.existsByEmail("v@mail.ru")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(10L);           // имитируем БД
            return u;
        });

        UserDto saved = service.create(dto);

        assertThat(saved.getId()).isEqualTo(10L);
        verify(repo).save(argThat(u -> u.getName().equals("Vasya")));
    }

    @Test
    @DisplayName("create(): дублирующая почта ⇒ IllegalStateException")
    void createDuplicateEmail() {
        when(repo.existsByEmail("dup@mail.ru")).thenReturn(true);

        UserDto dto = new UserDto();
        dto.setName("N");
        dto.setEmail("dup@mail.ru");

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalStateException.class);
        verify(repo, never()).save(any());
    }

    private User persisted() {
        return new User(5L, "Old", "old@mail.ru");
    }

    @Test
    @DisplayName("update(): меняем name и email")
    void updateChangeNameAndEmail() {
        when(repo.findById(5L)).thenReturn(Optional.of(persisted()));
        when(repo.existsByEmail("new@mail.ru")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = new UserDto();
        patch.setName("NewName");
        patch.setEmail("new@mail.ru");

        UserDto out = service.update(5L, patch);

        assertThat(out.getName()).isEqualTo("NewName");
        assertThat(out.getEmail()).isEqualTo("new@mail.ru");
    }

    @Test
    @DisplayName("update(): новый email уже занят ⇒ IllegalStateException")
    void updateEmailBusy() {
        when(repo.findById(5L)).thenReturn(Optional.of(persisted()));
        when(repo.existsByEmail("busy@mail.ru")).thenReturn(true);

        UserDto patch = new UserDto();
        patch.setEmail("busy@mail.ru");

        assertThatThrownBy(() -> service.update(5L, patch))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("update(): пользователь не найден ⇒ NoSuchElementException")
    void updateUserNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UserDto()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByIdReturnsDto() {
        when(repo.findById(5L)).thenReturn(Optional.of(persisted()));

        UserDto dto = service.findById(5L);

        assertThat(dto.getEmail()).isEqualTo("old@mail.ru");
    }

    @Test
    void findEntityByIdNotFound() {
        when(repo.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findEntityById(7L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findAllMapsEntities() {
        when(repo.findAll()).thenReturn(List.of(
                new User(1L, "A", "a@x"),
                new User(2L, "B", "b@x")
        ));

        List<UserDto> list = service.findAll();

        assertThat(list).hasSize(2)
                .extracting(UserDto::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void deleteInvokesRepository() {
        service.delete(3L);
        verify(repo).deleteById(3L);
    }
}
