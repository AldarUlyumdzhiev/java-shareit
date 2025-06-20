package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toCommentAndResponseDto() {
        // вход
        User author = User.builder().id(9L).name("Alice").build();
        Item item = Item.builder().id(3L).name("Отвертка").build();

        CommentRequestDto req = new CommentRequestDto();
        req.setText("Полезная вещь");

        //  toComment
        Comment comment = CommentMapper.toComment(item, req, author);
        assertThat(comment.getText()).isEqualTo("Полезная вещь");
        assertThat(comment.getAuthor().getName()).isEqualTo("Alice");

        // toCommentResponseDto
        comment.setId(77L);
        CommentResponseDto resp = CommentMapper.toCommentResponseDto(comment);

        assertThat(resp.getId()).isEqualTo(77L);
        assertThat(resp).extracting(CommentResponseDto::getAuthorName, CommentResponseDto::getText)
                .containsExactly("Alice", "Полезная вещь");
    }
}
