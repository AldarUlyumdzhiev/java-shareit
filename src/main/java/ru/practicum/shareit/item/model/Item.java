package ru.practicum.shareit.item.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    Integer id;
    String name;
    String description;
    Boolean available;
    User owner;
    ItemRequest request;
}
