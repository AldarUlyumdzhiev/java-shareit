package ru.practicum.shareit.request;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplUnitTest {

    @Mock ItemRequestRepository requestRepo;
    @Mock ItemRepository        itemRepo;
    @Mock UserServiceImpl       userService;

    @InjectMocks ItemRequestServiceImpl service;

    final User user = new User(1L, "User", "u@mail.ru");

    ItemRequest makeReq(Long id) {
        return ItemRequest.builder()
                .id(id).description("need").created(LocalDateTime.now()).requestor(user).build();
    }

    @BeforeEach void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void create_returnsSavedDto() {
        when(userService.findEntityById(1L)).thenReturn(user);

        ArgumentCaptor<ItemRequest> captor = ArgumentCaptor.forClass(ItemRequest.class);
        when(requestRepo.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Нужна дрель");

        ItemRequestDto out = service.create(dto, 1L);

        assertThat(out.getDescription()).isEqualTo("Нужна дрель");
        assertThat(captor.getValue().getRequestor()).isSameAs(user);
    }

    @Test
    void getAllRequestsByUser_emptyList() {
        when(userService.findEntityById(1L)).thenReturn(user);
        when(requestRepo.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of());

        assertThat(service.getAllRequestsByUser(1L)).isEmpty();
        verify(itemRepo, never()).findByRequestIdIn(any());
    }

    @Test
    void getAllRequestsByUser_withItems() {
        ItemRequest req = makeReq(10L);
        Item item = Item.builder().id(99L).name("Дрель").description("d")
                .available(true).owner(user).request(req).build();

        when(userService.findEntityById(1L)).thenReturn(user);
        when(requestRepo.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(req));
        when(itemRepo.findByRequestIdIn(List.of(10L))).thenReturn(List.of(item));

        List<ItemRequestDto> list = service.getAllRequestsByUser(1L);

        assertThat(list).singleElement()
                .extracting(ItemRequestDto::getItems)
                .asList()
                .hasSize(1);
    }

    @Test
    void getAllUserRequests_paginationAndFilter() {
        ItemRequest other = makeReq(20L);
        Page<ItemRequest> page = new PageImpl<>(List.of(other));

        when(userService.findEntityById(1L)).thenReturn(user);
        when(requestRepo.findByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(itemRepo.findByRequestIdIn(List.of(20L))).thenReturn(List.of());

        List<ItemRequestDto> list = service.getAllUserRequests(1L, 0, 10);

        assertThat(list).hasSize(1);
        verify(requestRepo).findByRequestorIdNot(eq(1L),
                argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10));
    }

    @Test
    void getById_notFound_throws404() {
        when(userService.findEntityById(1L)).thenReturn(user);
        when(requestRepo.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(5L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getById_success_withItems() {
        ItemRequest req = makeReq(30L);
        when(userService.findEntityById(1L)).thenReturn(user);
        when(requestRepo.findById(30L)).thenReturn(Optional.of(req));
        when(itemRepo.findByRequestId(30L))
                .thenReturn(List.of(Item.builder().id(111L).name("Ключ")
                        .description("d").available(true)
                        .owner(user).request(req).build()));

        ItemRequestDto dto = service.getById(30L, 1L);

        assertThat(dto.getId()).isEqualTo(30L);
        assertThat(dto.getItems()).hasSize(1);
    }
}
