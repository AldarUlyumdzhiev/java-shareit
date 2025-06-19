package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class ItemRequestServiceImplIT {

    @Autowired private ItemRequestService requestService;
    @Autowired private UserRepository userRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemRequestRepository itemRequestRepository;

    // create
    @Test
    @DisplayName("create(): сохраняет запрос с датой и описанием")
    void create_request() {
        User requester = userRepository.save(
                User.builder().name("Ann").email("ann@mail.ru").build());

        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Нужна газонокосилка");

        ItemRequestDto saved = requestService.create(dto, requester.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Нужна газонокосилка");
        assertThat(saved.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(saved.getItems()).isEmpty();
    }


    // getAllRequestsByUser
    @Test
    void getAllRequestsByUser_returnsOwnRequests() {
        User user = userRepository.save(
                User.builder().name("Bob").email("bob@mail.ru").build());

        CreateItemRequestDto r1 = new CreateItemRequestDto();
        r1.setDescription("Старый запрос");
        requestService.create(r1, user.getId());

        CreateItemRequestDto r2 = new CreateItemRequestDto();
        r2.setDescription("Новый запрос");
        requestService.create(r2, user.getId());

        List<ItemRequestDto> list = requestService.getAllRequestsByUser(user.getId());

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getDescription()).isEqualTo("Новый запрос");
        assertThat(list.get(1).getDescription()).isEqualTo("Старый запрос");
    }


    // getAllUserRequests
    @Test
    void getAllUserRequests_pagination() {
        User user1 = userRepository.save(
                User.builder().name("TestUser1").email("user1@test.com").build());
        User user2 = userRepository.save(
                User.builder().name("TestUser2").email("user2@test.com").build());

        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("req1");
        requestService.create(dto, user2.getId());

        dto.setDescription("req2");
        requestService.create(dto, user2.getId());

        List<ItemRequestDto> page =
                requestService.getAllUserRequests(user1.getId(), 0, 1);

        assertThat(page).hasSize(1);
        assertThat(page.get(0).getDescription()).isEqualTo("req2");
    }


    // getById
    @Test
    @DisplayName("getById(): возвращает запрос вместе с ответными вещами")
    void getById_withItems() {
        User requester = userRepository.save(
                User.builder().name("TestRequester").email("requester@test.com").build());
        User owner = userRepository.save(
                User.builder().name("TestOwner").email("owner@test.com").build());

        CreateItemRequestDto dto = new CreateItemRequestDto();
        dto.setDescription("Нужна дрель");
        ItemRequestDto reqSaved = requestService.create(dto, requester.getId());

        ItemRequest reqEntity = itemRequestRepository.findById(reqSaved.getId())
                .orElseThrow();

        itemRepository.save(
                Item.builder()
                        .name("Дрель")
                        .description("ударная")
                        .available(true)
                        .owner(owner)
                        .request(reqEntity)
                        .build());

        ItemRequestDto withItems = requestService.getById(reqSaved.getId(), requester.getId());

        assertThat(withItems.getItems()).hasSize(1)
                .extracting(i -> i.getName())
                .containsExactly("Дрель");
    }
}
