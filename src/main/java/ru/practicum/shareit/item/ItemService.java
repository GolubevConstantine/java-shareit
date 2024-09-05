package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.item.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

public interface ItemService {
    ItemDtoResponse getItemById(long itemId, long userId);

    List<ItemDtoResponse> getItemsByOwner(long userId);

    List<ItemDtoResponse> getItemBySearch(String text);

    ItemDtoResponse saveNewItem(ItemDtoRequest itemDtoRequest, long userId);

    ItemDtoResponse updateItem(long itemId, ItemDtoRequest itemDtoRequest, long userId);

    CommentDtoResponse saveNewComment(long itemId, CommentDtoRequest commentDtoRequest, long userId);
}
