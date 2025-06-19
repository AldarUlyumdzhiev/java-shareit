package ru.practicum.shareit.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserServiceImpl userService;

    @Override
    public ItemRequestDto create(CreateItemRequestDto dto, Long userId) {
        User user = userService.findEntityById(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, user);
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByUser(Long userId) {
        userService.findEntityById(userId); // проверка существования
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return toDtoListWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Long userId, int from, int size) {
        userService.findEntityById(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findByRequestorIdNot(userId, pageable).getContent();
        return toDtoListWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long requestId, Long userId) {
        userService.findEntityById(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found"));
        List<Item> items = itemRepository.findByRequestId(request.getId());
        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> toDtoListWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(req -> ItemRequestMapper.toItemRequestDto(req, itemsByRequest.getOrDefault(req.getId(), List.of())))
                .collect(Collectors.toList());
    }
}

