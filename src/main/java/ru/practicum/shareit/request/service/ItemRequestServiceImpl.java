package ru.practicum.shareit.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserServiceImpl userService;

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        User requestor = userService.findEntityById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(dto, requestor);
        ItemRequest saved = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(saved);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByUser(Long userId) {
        List<ItemRequestDto> allRequestsByUser = itemRequestRepository.findAllByRequestorId(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        return allRequestsByUser;
    }

    public ItemRequestDto getById(Long requestId, Long userId) {
        ItemRequest itemRequest = itemRequestRepository.getByIdAndRequestorId(requestId, userId);
        if (itemRequest == null) {
            throw new NoSuchElementException("Item request not found");
        }
        ItemRequestDto dto = ItemRequestMapper.toItemRequestDto(itemRequest);
        return dto;
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Long userId) {
        List<ItemRequestDto> allUserRequests = itemRequestRepository.findAllByRequestorId(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
        return allUserRequests;

    }
}
