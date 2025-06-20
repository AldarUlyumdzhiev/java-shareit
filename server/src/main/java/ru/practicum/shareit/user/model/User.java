package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_email", columnNames = "email")
})
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String name;

    @Column(nullable = false, length = 512, unique = true)
    String email;
}
