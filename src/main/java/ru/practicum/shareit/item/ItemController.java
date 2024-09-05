package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.item.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.utils.Create;
import ru.practicum.shareit.utils.Update;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDtoResponse saveNewItem(@Validated(Create.class) @RequestBody ItemDtoRequest itemDtoRequest,
                                       @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("POST / items {} / user {}", itemDtoRequest.getName(), userId);
        return itemService.saveNewItem(itemDtoRequest, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse updateItem(@PathVariable long itemId,
                                      @Validated(Update.class) @RequestBody ItemDtoRequest itemDtoRequest,
                                      @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("PATCH / items {} / user {}", itemId, userId);
        return itemService.updateItem(itemId, itemDtoRequest, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getItemById(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET / items {} / user {}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoResponse> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET / items / user {}", userId);
        return itemService.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> getItemsBySearch(@RequestParam String text) {
        log.info("GET / search / {}", text);
        return itemService.getItemBySearch(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse addComment(@PathVariable long itemId,
                                         @Validated(Create.class) @RequestBody CommentDtoRequest commentDtoRequest,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("POST / comment / item {}", itemId);
        return itemService.saveNewComment(itemId, commentDtoRequest, userId);
    }
}
