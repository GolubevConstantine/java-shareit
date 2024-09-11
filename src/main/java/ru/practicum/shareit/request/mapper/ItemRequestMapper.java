package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

@UtilityClass
public class ItemRequestMapper {
    public ItemRequest toItemRequest(ItemRequestDtoRequest requestDtoIn) {
        return new ItemRequest(
                requestDtoIn.getDescription()
        );
    }

    public ItemRequestDtoResponse toItemRequestDtoResponse(ItemRequest request) {
        return new ItemRequestDtoResponse(
                request.getId(),
                request.getDescription(),
                request.getRequestor().getId(),
                request.getCreated()
        );
    }
}