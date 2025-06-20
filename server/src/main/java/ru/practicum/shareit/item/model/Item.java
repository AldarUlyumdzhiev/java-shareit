package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;


@Data
@Entity
@Table(name = "items")
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 255, nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "is_available", nullable = false)
    Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = true)
    ItemRequest request;
}
