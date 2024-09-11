package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDtoResponse saveNewRequest(ItemRequestDtoRequest requestDtoIn, long userId) {
        log.info("Создание нового запроса {}", requestDtoIn.getDescription());
        User requestor = getUser(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(requestDtoIn);
        request.setCreated(LocalDateTime.now());
        request.setRequestor(requestor);
        return ItemRequestMapper.toItemRequestDtoResponse(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDtoResponse> getRequestsByRequestor(long userId) {
        log.info("Получение всех запросов по просителю с идентификатором {}", userId);
        getUser(userId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorId(userId, Sort.by(DESC, "created"));
        return addItems(requests);
    }

    @Override
    public List<ItemRequestDtoResponse> getAllRequests(Integer from, Integer size, long userId) {
        log.info("Получение всех запросов постранично");
        getUser(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdIsNot(userId, pageable);
        return addItems(requests);
    }

    @Override
    public ItemRequestDtoResponse getRequestById(long requestId, long userId) {
        log.info("Получение запроса по идентификатору {}", requestId);
        getUser(userId);
        ItemRequestDtoResponse requestDtoOut = ItemRequestMapper.toItemRequestDtoResponse(requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new EntityNotFoundException(String.format("Объект класса %s не найден", ItemRequest.class))));
        requestDtoOut.setItems(itemRepository.findAllByRequestId(requestId).stream()
                .map(ItemMapper::toItemDtoResponse).collect(Collectors.toList()));
        return requestDtoOut;
    }

    private List<ItemRequestDtoResponse> addItems(List<ItemRequest> requests) {
        final List<ItemRequestDtoResponse> requestsOut = new ArrayList<>();
        for (ItemRequest request : requests) {
            ItemRequestDtoResponse requestDtoOut = ItemRequestMapper.toItemRequestDtoResponse(request);
            List<ItemDtoResponse> items = itemRepository.findAllByRequestId(request.getId()).stream()
                    .map(ItemMapper::toItemDtoResponse).collect(Collectors.toList());
            requestDtoOut.setItems(items);
            requestsOut.add(requestDtoOut);
        }
        return requestsOut;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", User.class)));
    }
}
