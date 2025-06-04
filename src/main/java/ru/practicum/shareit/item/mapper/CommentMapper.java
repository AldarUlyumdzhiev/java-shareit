package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(comment.getId());
        responseDto.setText(comment.getText());
        responseDto.setAuthorName(comment.getAuthor().getName());
        responseDto.setCreated(comment.getCreated());
        return responseDto;
    }

    public static Comment toComment(Item item, CommentRequestDto requestDto, User author) {
        Comment comment = new Comment();
        comment.setText(requestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}
