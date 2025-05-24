package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);
    UserDto update(Integer id, UserDto userDto);
    UserDto findById(Integer id);
    List<UserDto> findAll();
    void delete(Integer id);
}
