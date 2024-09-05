package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.utils.Create;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoResponse saveNewBooking(@Validated(Create.class) @RequestBody BookingDtoRequest bookingDtoRequest,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("POST / bookings");
        return bookingService.saveNewBooking(bookingDtoRequest, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@PathVariable long bookingId, @RequestParam(name = "approved") Boolean isApproved,
                                      @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("PATCH / bookings / {}", bookingId);
        return bookingService.approve(bookingId, isApproved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBookingById(@PathVariable long bookingId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET / bookings / {}", bookingId);
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> getAllByBooker(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                   @RequestHeader("X-Sharer-User-Id") long bookerId) {
        log.info("GET / ByBooker {}", bookerId);
        return bookingService.getAllByBooker(state, bookerId);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllByOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                                  @RequestHeader("X-Sharer-User-Id") long ownerId) {
        log.info("GET / ByOwner / {}", ownerId);
        return bookingService.getAllByOwner(ownerId, state);
    }
}
