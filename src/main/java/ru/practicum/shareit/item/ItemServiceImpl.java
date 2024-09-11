package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.*;
import ru.practicum.shareit.item.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.item.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDtoResponse saveNewItem(ItemDtoRequest itemDtoRequest, long userId) {
        log.info("Создание новой вещи {}", itemDtoRequest.getName());
        User owner = getUser(userId);
        Item item = ItemMapper.toItem(itemDtoRequest);
        item.setOwner(owner);
        Long requestId = itemDtoRequest.getRequestId();
        if (requestId != null) {
            item.setRequest(requestRepository.findById(requestId).orElseThrow(() ->
                    new EntityNotFoundException(String.format("Объект класса %s не найден", ItemRequest.class))));
        }
        return ItemMapper.toItemDtoResponse(itemRepository.save(item));
    }

    @Override
    public ItemDtoResponse updateItem(long itemId, ItemDtoRequest itemDtoRequest, long userId) {
        log.info("Обновление вещи {} с идентификатором {}", itemDtoRequest.getName(), itemId);
        getUser(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", Item.class)));
        String name = itemDtoRequest.getName();
        String description = itemDtoRequest.getDescription();
        Boolean available = itemDtoRequest.getAvailable();
        if (item.getOwner().getId() == userId) {
            if (name != null && !name.isBlank()) {
                item.setName(name);
            }
            if (description != null && !description.isBlank()) {
                item.setDescription(description);
            }
            if (available != null) {
                item.setAvailable(available);
            }
        } else {
            throw new ForbiddenException(String.format("Пользователь с id %s не является собственником %s",
                    userId, name));
        }
        return ItemMapper.toItemDtoResponse(item);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDtoResponse getItemById(long itemId, long userId) {
        log.info("Получение вещи по идентификатору {}", itemId);
        return itemRepository.findById(itemId).map(item -> addBookingsAndComments(item, userId)).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", Item.class)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDtoResponse> getItemsByOwner(long userId) {
        log.info("Получение вещи по владельцу {}", userId);
        getUser(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return addBookingsAndCommentsForList(items);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDtoResponse> getItemBySearch(String text) {
        log.info("Получение вещи по поиску {}", text);
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream().map(ItemMapper::toItemDtoResponse).collect(toList());
    }

    @Override
    public CommentDtoResponse saveNewComment(long itemId, CommentDtoRequest commentDtoRequest, long userId) {
        User user = getUser(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", Item.class)));
        if (!bookingRepository.existsByBookerIdAndItemIdAndEndBefore(user.getId(), item.getId(), LocalDateTime.now())) {
            throw new ValidationException("Пользователь не пользовался вещью");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentDtoRequest, item, user));
        return CommentMapper.toCommentDtoResponse(comment);
    }

    private ItemDtoResponse addBookingsAndComments(Item item, long userId) {
        ItemDtoResponse itemDtoResponse = ItemMapper.toItemDtoResponse(item);

        LocalDateTime thisMoment = LocalDateTime.now();
        if (itemDtoResponse.getOwner().getId() == userId) {
            itemDtoResponse.setLastBooking(bookingRepository
                    .findFirstByItemIdAndStartLessThanEqualAndStatus(itemDtoResponse.getId(), thisMoment,
                            BookingStatus.APPROVED, Sort.by(DESC, "end"))
                    .map(BookingMapper::toBookingDtoShort)
                    .orElse(null));

            itemDtoResponse.setNextBooking(bookingRepository
                    .findFirstByItemIdAndStartAfterAndStatus(itemDtoResponse.getId(), thisMoment,
                            BookingStatus.APPROVED, Sort.by(ASC, "end"))
                    .map(BookingMapper::toBookingDtoShort)
                    .orElse(null));
        }

        itemDtoResponse.setComments(commentRepository.findAllByItemId(itemDtoResponse.getId())
                .stream()
                .map(CommentMapper::toCommentDtoResponse)
                .collect(toList()));

        return itemDtoResponse;
    }

    private List<ItemDtoResponse> addBookingsAndCommentsForList(List<Item> items) {
        LocalDateTime thisMoment = LocalDateTime.now();

        Map<Item, Booking> itemsWithLastBookings = bookingRepository
                .findByItemInAndStartLessThanEqualAndStatus(items, thisMoment,
                        BookingStatus.APPROVED, Sort.by(DESC, "end"))
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (o1, o2) -> o1));

        Map<Item, Booking> itemsWithNextBookings = bookingRepository
                .findByItemInAndStartAfterAndStatus(items, thisMoment,
                        BookingStatus.APPROVED, Sort.by(ASC, "end"))
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (o1, o2) -> o1));

        Map<Item, List<Comment>> itemsWithComments = commentRepository
                .findByItemIn(items, Sort.by(DESC, "created"))
                .stream()
                .collect(groupingBy(Comment::getItem, toList()));

        List<ItemDtoResponse> itemDtoResponses = new ArrayList<>();
        for (Item item : items) {
            ItemDtoResponse itemDtoResponse = ItemMapper.toItemDtoResponse(item);
            Booking lastBooking = itemsWithLastBookings.get(item);
            if (!itemsWithLastBookings.isEmpty() && lastBooking != null) {
                itemDtoResponse.setLastBooking(BookingMapper.toBookingDtoShort(lastBooking));
            }
            Booking nextBooking = itemsWithNextBookings.get(item);
            if (!itemsWithNextBookings.isEmpty() && nextBooking != null) {
                itemDtoResponse.setNextBooking(BookingMapper.toBookingDtoShort(nextBooking));
            }
            List<CommentDtoResponse> commentDtoResponses = itemsWithComments.getOrDefault(item, Collections.emptyList())
                    .stream()
                    .map(CommentMapper::toCommentDtoResponse)
                    .collect(toList());
            itemDtoResponse.setComments(commentDtoResponses);

            itemDtoResponses.add(itemDtoResponse);
        }
        return itemDtoResponses;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("Объект класса %s не найден", User.class)));
    }
}
