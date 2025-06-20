package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalStateException("User with this email already exists");
        }
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new IllegalStateException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return UserMapper.toUserDto(user);
    }

    public User findEntityById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return user;
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
