package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Map<Integer, User> storage = new HashMap<>();

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        boolean emailExists = storage.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()));
        if (emailExists) {
            throw new IllegalStateException("User with this email already exists");
        }

        int newId = storage.keySet().stream().max(Integer::compareTo).orElse(-1) + 1;
        user.setId(newId);
        storage.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }



    @Override
    public UserDto update(Integer id, UserDto userDto) {
        User user = storage.get(id);
        if (user == null) throw new NoSuchElementException("User not found");

        if (userDto.getEmail() != null) {
            boolean emailExists = storage.values().stream()
                    .anyMatch(u -> !u.getId().equals(id) && u.getEmail().equalsIgnoreCase(userDto.getEmail()));
            if (emailExists) {
                throw new IllegalStateException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(user);
    }


    @Override
    public UserDto findById(Integer id) {
        User user = storage.get(id);
        if (user == null) throw new NoSuchElementException("User not found");
        return UserMapper.toUserDto(user);
    }

    public User findEntityById(Integer id) {
        User user = storage.get(id);
        if (user == null) throw new NoSuchElementException("User not found");
        return user;
    }

    @Override
    public List<UserDto> findAll() {
        return storage.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id) {
        storage.remove(id);
    }
}
