package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoResponse saveNewRequest(ItemRequestDtoRequest requestDtoIn, long userId);

    List<ItemRequestDtoResponse> getRequestsByRequestor(long userId);

    List<ItemRequestDtoResponse> getAllRequests(Integer from, Integer size, long userId);

    ItemRequestDtoResponse getRequestById(long requestId, long userId);
}
