package ru.practicum.shareit.item.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDtoResponse {
    private long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}
