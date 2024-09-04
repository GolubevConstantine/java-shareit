package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoShort;
import ru.practicum.shareit.user.mapper.UserMapper;

@UtilityClass
public class ItemMapper {
    public ItemDtoResponse toItemDtoResponse(Item item) {
        return new ItemDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                UserMapper.toUserDtoShort(item.getOwner())
        );
    }

    public ItemDtoShort toItemDtoShort(Item item) {
        return new ItemDtoShort(
                item.getId(),
                item.getName()
        );
    }

    public Item toItem(ItemDtoRequest itemDtoRequest) {
        return new Item(
                itemDtoRequest.getName(),
                itemDtoRequest.getDescription(),
                itemDtoRequest.getAvailable()
        );
    }
}
