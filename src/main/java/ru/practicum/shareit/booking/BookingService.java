package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {
    BookingDtoResponse saveNewBooking(BookingDtoRequest bookingDtoRequest, long userId);

    BookingDtoResponse approve(long bookingId, Boolean isApproved, long userId);

    BookingDtoResponse getBookingById(long bookingId, long userId);

    List<BookingDtoResponse> getAllByBooker(String subState, long bookerId);

    List<BookingDtoResponse> getAllByOwner(long ownerId, String state);
}

