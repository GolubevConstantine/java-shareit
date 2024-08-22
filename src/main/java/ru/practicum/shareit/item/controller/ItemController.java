package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.utils.Create;
import ru.practicum.shareit.utils.Update;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        log.info("GET / items / {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET / items");
        return itemService.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemBySearch(@RequestParam String text) {
        log.info("GET / search / {}", text);
        return itemService.getItemsBySearch(text);
    }

    @PostMapping
    public ItemDto saveNewItem(@Validated(Create.class) @RequestBody ItemDto itemDto,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("POST / items / {}", itemDto.getName());
        itemDto.setOwnerId(userId);
        return itemService.saveNewItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Validated(Update.class) @PathVariable long itemId, @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("PATCH / items / {}", itemId);
        itemDto.setOwnerId(userId);
        itemDto.setId(itemId);
        return itemService.updateItem(itemDto);
    }
}
