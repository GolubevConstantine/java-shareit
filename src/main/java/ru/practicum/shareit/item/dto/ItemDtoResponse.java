package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoShort;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemDtoResponse {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private List<CommentDtoResponse> comments;
    private UserDtoShort owner;

    public ItemDtoResponse(long id, String name, String description, Boolean available, UserDtoShort owner) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.owner = owner;
    }
}
